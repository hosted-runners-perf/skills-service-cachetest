/**
 * Copyright 2020 SkillTree
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package skills.intTests.copyProject

import skills.intTests.utils.DefaultIntSpec
import skills.intTests.utils.SkillsFactory
import skills.intTests.utils.SkillsService
import skills.services.admin.skillReuse.SkillReuseIdUtil
import skills.services.settings.Settings
import skills.skillLoading.RankingLoader
import skills.storage.model.SkillDef
import skills.storage.model.auth.RoleName
import static skills.intTests.utils.SkillsFactory.*

class CopyProjectSpecs extends DefaultIntSpec {

    def "copy project with majority of features utilized"() {
        def p1 = createProject(1)
        skillsService.createProject(p1)

        // edit levels
        def levels = skillsService.getLevels(p1.projectId, null).sort() { it.level }
        levels.each { def levelToEdit ->
            levelToEdit.percent = levelToEdit.percent + 1
            skillsService.editLevel(p1.projectId, null, levelToEdit.level as String, levelToEdit)
        }
        def projLevels = skillsService.getLevels(p1.projectId, null).sort() { it.level }

        def p1subj1 = createSubject(1, 1)
        def p1Skills = createSkills(10, 1, 1, 100)
        def group1 = createSkillsGroup(1, 1, 22)
        def group2 = createSkillsGroup(1, 1, 33)
        skillsService.createSubject(p1subj1)
        skillsService.createSkills([p1Skills[0..2], group1, group2].flatten())

        p1Skills[3..7].each {
            skillsService.assignSkillToSkillsGroup(group1.skillId, it)
        }
        p1Skills[8..9].each {
            skillsService.assignSkillToSkillsGroup(group2.skillId, it)
        }

        def p1subj2 = createSubject(1, 2)
        def p1SkillsSubj2 = createSkills(5, 1, 2, 22)
        def group3 = createSkillsGroup(1, 2, 33)
        skillsService.createSubject(p1subj2)

        // edit subject
        def subj1Level = skillsService.getLevels(p1.projectId, p1subj2.subjectId).sort() { it.level }
        subj1Level.each { def levelToEdit ->
            levelToEdit.percent = levelToEdit.percent + 2
            skillsService.editLevel(p1.projectId, p1subj1.subjectId, levelToEdit.level as String, levelToEdit)
        }

        skillsService.createSkills([p1SkillsSubj2, group3].flatten())

        def badge = SkillsFactory.createBadge(1, 50)
        skillsService.createBadge(badge)
        skillsService.assignSkillToBadge(p1.projectId, badge.badgeId, p1Skills[0].skillId)
        skillsService.assignSkillToBadge(p1.projectId, badge.badgeId, p1Skills[1].skillId)
        skillsService.assignSkillToBadge(p1.projectId, badge.badgeId, p1SkillsSubj2[1].skillId)

        skillsService.assignDependency([projectId: p1.projectId, skillId: p1Skills.get(0).skillId, dependentSkillId: p1Skills.get(2).skillId])
        skillsService.assignDependency([projectId: p1.projectId, skillId: p1Skills.get(2).skillId, dependentSkillId: p1SkillsSubj2.get(2).skillId])

        skillsService.reuseSkills(p1.projectId, [p1Skills[1].skillId], p1subj2.subjectId)
        skillsService.reuseSkills(p1.projectId, [p1Skills[7].skillId], p1subj2.subjectId, group3.skillId)

        def subj1UpdatedLevels = skillsService.getLevels(p1.projectId, p1subj1.subjectId).sort() { it.level }
        def subj2UpdatedLevels = skillsService.getLevels(p1.projectId, p1subj2.subjectId).sort() { it.level }

        when:
        def projToCopy = createProject(2)
        skillsService.copyProject(p1.projectId, projToCopy)

        def copiedProjStats = skillsService.getProject(projToCopy.projectId)
        def copiedSubject1 = skillsService.getSubject([subjectId: p1subj1.subjectId, projectId: projToCopy.projectId])
        def copiedSubj1Skills = skillsService.getSkillsForSubject(projToCopy.projectId, p1subj1.subjectId)
        def copiedGroup1Skills = skillsService.getSkillsForGroup(projToCopy.projectId, group1.skillId)
        def copiedGroup2Skills = skillsService.getSkillsForGroup(projToCopy.projectId, group2.skillId)

        def copiedSubject2 = skillsService.getSubject([subjectId: p1subj2.subjectId, projectId: projToCopy.projectId])
        def copiedSubj2Skills = skillsService.getSkillsForSubject(projToCopy.projectId, p1subj2.subjectId)
        def copiedGroup3Skills = skillsService.getSkillsForGroup(projToCopy.projectId, group3.skillId)

        def copiedBadge = skillsService.getBadge(projToCopy.projectId, badge.badgeId)
        def copiedDeps = skillsService.getDependencyGraph(projToCopy.projectId)

        def copiedProjLevels = skillsService.getLevels(projToCopy.projectId, null).sort() { it.level }
        def copiedSubj1UpdatedLevels = skillsService.getLevels(p1.projectId, p1subj1.subjectId).sort() { it.level }
        def copiedSubj2UpdatedLevels = skillsService.getLevels(p1.projectId, p1subj2.subjectId).sort() { it.level }

        then:
        copiedProjStats.name == projToCopy.name
        copiedProjStats.numSubjects == 2
        copiedProjStats.numSkills == 15
        copiedProjStats.totalPoints == (100 * 10) + (5 * 22)
        copiedProjStats.numSkillsReused == 2
        copiedProjStats.totalPointsReused == 200

        copiedSubject1.name == p1subj1.name
        copiedSubject1.subjectId == p1subj1.subjectId
        copiedSubject1.numGroups == 2
        copiedSubject1.numSkills == 10
        copiedSubject1.totalPoints == (100 * 10)
        copiedSubject1.numSkillsReused == 0
        copiedSubject1.totalPointsReused == 0

        copiedSubj1Skills.skillId == [p1Skills[0..2], group1, group2].flatten().skillId
        copiedGroup1Skills.skillId == p1Skills[3..7].skillId
        copiedGroup2Skills.skillId == p1Skills[8..9].skillId

        copiedSubject2.name == p1subj2.name
        copiedSubject2.subjectId == p1subj2.subjectId
        copiedSubject2.numGroups == 1
        copiedSubject2.numSkills == 5
        copiedSubject2.totalPoints == (22 * 5)
        copiedSubject2.numSkillsReused == 2
        copiedSubject2.totalPointsReused == 200

        copiedSubj2Skills.skillId == [[p1SkillsSubj2, group3].flatten().skillId, SkillReuseIdUtil.addTag(p1Skills[1].skillId, 0)].flatten()
        copiedGroup3Skills.skillId == [SkillReuseIdUtil.addTag(p1Skills[7].skillId, 0)]

        copiedBadge.name == badge.name
        copiedBadge.projectId == projToCopy.projectId
        copiedBadge.badgeId == badge.badgeId
        copiedBadge.totalPoints == (100 * 2) + (22 * 1)
        copiedBadge.numSkills == 3

        validateGraph(copiedDeps, [
                new Edge(from: p1Skills[0].skillId, to: p1Skills[2].skillId),
                new Edge(from: p1Skills[2].skillId, to: p1SkillsSubj2[2].skillId),
        ])

        copiedProjLevels.percent == projLevels.percent

        copiedSubj1UpdatedLevels.percent == subj1UpdatedLevels.percent
        copiedSubj2UpdatedLevels.percent == subj2UpdatedLevels.percent
    }

    def "subject attributes are properly copied"() {
        def p1 = createProject(1)
        def p1subj1 = createSubject(1, 1)
        p1subj1.description = "Very important Stuff"
        p1subj1.helpUrl = "http://www.greatlink.com"
        p1subj1.iconClass = "fas fa-address-card"
        skillsService.createProjectAndSubjectAndSkills(p1, p1subj1, [])

        when:
        def projToCopy = createProject(2)
        skillsService.copyProject(p1.projectId, projToCopy)

        def copiedSubjs = skillsService.getSubjects(projToCopy.projectId)
        then:
        copiedSubjs.subjectId == [p1subj1.subjectId]
        def copiedSubj = copiedSubjs[0]
        copiedSubj.name == p1subj1.name
        copiedSubj.description == p1subj1.description
        copiedSubj.helpUrl == p1subj1.helpUrl
        copiedSubj.iconClass == p1subj1.iconClass
    }

    def "subjects display order is copied"() {
        def p1 = createProject(1)
        def p1subj1 = createSubject(1, 1)
        def p1subj2 = createSubject(1, 2)
        def p1subj3 = createSubject(1, 3)
        skillsService.createProjectAndSubjectAndSkills(p1, p1subj1, [])
        skillsService.createSubject(p1subj2)
        skillsService.createSubject(p1subj3)

        skillsService.changeSubjectDisplayOrder(p1subj3, 0)

        when:
        def originalSubjs = skillsService.getSubjects(p1.projectId).sort { it.displayOrder }
        def projToCopy = createProject(2)
        skillsService.copyProject(p1.projectId, projToCopy)

        def copiedSubjs = skillsService.getSubjects(projToCopy.projectId).sort { it.displayOrder }
        then:
        originalSubjs.subjectId == [p1subj3.subjectId, p1subj1.subjectId, p1subj2.subjectId]
        copiedSubjs.subjectId == [p1subj3.subjectId, p1subj1.subjectId, p1subj2.subjectId]
        copiedSubjs.displayOrder == [1, 2, 3]
    }

    def "settings are copied"() {
        def p1 = createProject(1)
        def p1subj1 = createSubject(1, 1)
        skillsService.createProjectAndSubjectAndSkills(p1, p1subj1, [])

        skillsService.addOrUpdateProjectSetting(p1.projectId, RankingLoader.PROJ_ADMINS_RANK_AND_LEADERBOARD_OPT_OUT_PREF, true.toString())
        skillsService.addOrUpdateProjectSetting(p1.projectId, "project.displayName", "blah")

        when:
        def projToCopy = createProject(2)
        skillsService.copyProject(p1.projectId, projToCopy)

        def copiedSettings = skillsService.getProjectSettings(projToCopy.projectId).sort { it.setting }
        then:
        copiedSettings.setting == [RankingLoader.PROJ_ADMINS_RANK_AND_LEADERBOARD_OPT_OUT_PREF, "project.displayName", Settings.USER_PROJECT_ROLE.settingName]
        copiedSettings.value == ["true", "blah", RoleName.ROLE_PROJECT_ADMIN.toString()]
        copiedSettings.projectId == [projToCopy.projectId, projToCopy.projectId, projToCopy.projectId]
    }

    def "copied project should not be discoverable by default"() {
        def p1 = createProject(1)
        def p1subj1 = createSubject(1, 1)
        skillsService.createProjectAndSubjectAndSkills(p1, p1subj1, [])

        skillsService.addOrUpdateProjectSetting(p1.projectId, RankingLoader.PROJ_ADMINS_RANK_AND_LEADERBOARD_OPT_OUT_PREF, true.toString())
        skillsService.addOrUpdateProjectSetting(p1.projectId, Settings.PRODUCTION_MODE.settingName, true.toString())
        skillsService.addOrUpdateProjectSetting(p1.projectId, "project.displayName", "blah")

        when:
        def projToCopy = createProject(2)
        skillsService.copyProject(p1.projectId, projToCopy)

        def copiedSettings = skillsService.getProjectSettings(projToCopy.projectId).sort { it.setting }
        then:
        copiedSettings.projectId == [projToCopy.projectId, projToCopy.projectId, projToCopy.projectId]
        copiedSettings.setting == [RankingLoader.PROJ_ADMINS_RANK_AND_LEADERBOARD_OPT_OUT_PREF, "project.displayName", Settings.USER_PROJECT_ROLE.settingName]
        copiedSettings.value == ["true", "blah", RoleName.ROLE_PROJECT_ADMIN.toString()]
    }

    def "group attributes are properly copied"() {
        def p1 = createProject(1)
        def p1subj1 = createSubject(1, 1)
        def skill1 = createSkill(1, 1, 22, 0, 12, 512, 18,)
        def skill2 = createSkill(1, 1, 23, 0, 12, 512, 18,)

        def group1 = createSkillsGroup(1, 1, 4)
        group1.description = "blah 1"
        skillsService.createProjectAndSubjectAndSkills(p1, p1subj1, [group1])
        skillsService.assignSkillToSkillsGroup(group1.skillId, skill1)
        skillsService.assignSkillToSkillsGroup(group1.skillId, skill2)
        group1.numSkillsRequired = 1
        skillsService.createSkill(group1)

        def group2 = createSkillsGroup(1, 1, 5)
        group2.description = "something else"
        skillsService.createSkill(group2)

        when:
        def projToCopy = createProject(2)
        skillsService.copyProject(p1.projectId, projToCopy)

        def original1 = skillsService.getSkill([projectId: p1.projectId, subjectId: p1subj1.subjectId, skillId: group1.skillId])
        def original2 = skillsService.getSkill([projectId: p1.projectId, subjectId: p1subj1.subjectId, skillId: group2.skillId])

        def copied1 = skillsService.getSkill([projectId: projToCopy.projectId, subjectId: p1subj1.subjectId, skillId: group1.skillId])
        def copied2 = skillsService.getSkill([projectId: projToCopy.projectId, subjectId: p1subj1.subjectId, skillId: group2.skillId])

        then:
        original1.description == group1.description
        copied1.description == group1.description
        copied1.skillId == group1.skillId
        copied1.name == group1.name
        copied1.type == SkillDef.ContainerType.SkillsGroup.toString()
        copied1.totalPoints == (18 * 12) * 2
        copied1.numSkillsRequired == 1

        original2.description == group2.description
        copied2.description == group2.description
        copied2.skillId == group2.skillId
        copied2.name == group2.name
        copied2.type == SkillDef.ContainerType.SkillsGroup.toString()
        copied2.totalPoints == 0
        copied2.numSkillsRequired == -1

    }

    def "group with multiple skills and partial requirement of most skills is properly copied"() {
        def p1 = createProject(1)
        def p1subj1 = createSubject(1, 1)
        def skills = createSkills(10, 1, 1, 100)
        def group1 = createSkillsGroup(1, 1, 40)
        skillsService.createProjectAndSubjectAndSkills(p1, p1subj1, [group1])
        skills.each {
            skillsService.assignSkillToSkillsGroup(group1.skillId, it)
        }
        group1.numSkillsRequired = 9
        skillsService.createSkill(group1)

        when:
        def projToCopy = createProject(2)
        skillsService.copyProject(p1.projectId, projToCopy)

        def original1 = skillsService.getSkill([projectId: p1.projectId, subjectId: p1subj1.subjectId, skillId: group1.skillId])

        def copied1 = skillsService.getSkill([projectId: projToCopy.projectId, subjectId: p1subj1.subjectId, skillId: group1.skillId])

        then:
        original1.description == group1.description
        copied1.description == group1.description
        copied1.skillId == group1.skillId
        copied1.name == group1.name
        copied1.type == SkillDef.ContainerType.SkillsGroup.toString()
        copied1.totalPoints == (10 * 100)
        copied1.numSkillsRequired == 9
    }

    def "badge properties are copied"() {
        def p1 = createProject(1)
        def p1subj1 = createSubject(1, 1)
        def skills = createSkills(6, 1, 1)
        skillsService.createProjectAndSubjectAndSkills(p1, p1subj1, skills)

        def badge1 = createBadge(1, 1)
        badge1.description = "blah 1"
        badge1.helpUrl = "http://www.greatlink.com"
        badge1.iconClass = "fas fa-adjust"
        skillsService.createBadge(badge1)
        skills[0..2].each {
            skillsService.assignSkillToBadge(p1.projectId, badge1.badgeId, it.skillId)
        }
        badge1.enabled = true
        skillsService.createBadge(badge1)

        def badge2 = createBadge(1, 2)
        badge2.description = "blah 1"
        badge2.helpUrl = "http://www.someotherlink.com"
        Date oneWeekAgo = new Date() - 7
        Date oneWeekInTheFuture = new Date() + 7
        badge2.startDate = oneWeekAgo
        badge2.endDate = oneWeekInTheFuture
        skillsService.createBadge(badge2)
        skills[2..5].each {
            skillsService.assignSkillToBadge(p1.projectId, badge2.badgeId, it.skillId)
        }

        when:
        def projToCopy = createProject(2)
        skillsService.copyProject(p1.projectId, projToCopy)

        def copied1 = skillsService.getBadge([projectId: projToCopy.projectId, badgeId: badge1.badgeId])
        def copied2 = skillsService.getBadge([projectId: projToCopy.projectId, badgeId: badge2.badgeId])

        then:
        copied1.description == badge1.description
        copied1.badgeId == badge1.badgeId
        copied1.name == badge1.name
        copied1.helpUrl == badge1.helpUrl
        copied1.numSkills == 3
        copied1.totalPoints == 10 * 3
        copied1.enabled == "true"
        copied1.iconClass == badge1.iconClass
        !copied1.startDate
        !copied1.endDate

        copied2.description == badge2.description
        copied2.badgeId == badge2.badgeId
        copied2.name == badge2.name
        copied2.helpUrl == badge2.helpUrl
        copied2.numSkills == 4
        copied2.totalPoints == 10 * 4
        copied2.enabled == "false"
        copied2.startDate == oneWeekAgo.format("MM-dd-yyy")
        copied2.endDate == oneWeekInTheFuture.format("MM-dd-yyy")
        copied2.iconClass == "fa fa-question-circle" // default
    }

    def "badge display order is copied"() {
        def p1 = createProject(1)
        def p1subj1 = createSubject(1, 1)
        def skills = createSkills(6, 1, 1)
        skillsService.createProjectAndSubjectAndSkills(p1, p1subj1, skills)

        def badge1 = createBadge(1, 1)
        skillsService.createBadge(badge1)
        def badge2 = createBadge(1, 2)
        skillsService.createBadge(badge2)
        def badge3 = createBadge(1, 3)
        skillsService.createBadge(badge3)

        skillsService.changeBadgeDisplayOrder(badge3, 0)

        when:
        def projToCopy = createProject(2)
        skillsService.copyProject(p1.projectId, projToCopy)

        def orig = skillsService.getBadges(p1.projectId).sort { it.displayOrder }
        def copied1 = skillsService.getBadges(projToCopy.projectId).sort { it.displayOrder }

        then:
        orig.badgeId == [badge3.badgeId, badge1.badgeId, badge2.badgeId]
        copied1.badgeId == [badge3.badgeId, badge1.badgeId, badge2.badgeId]
        copied1.displayOrder == [1, 2, 3]
    }

    def "copy live badge with no skills"() {
        def p1 = createProject(1)
        def p1subj1 = createSubject(1, 1)
        def skills = createSkills(6, 1, 1)
        skillsService.createProjectAndSubjectAndSkills(p1, p1subj1, skills)

        def badge1 = createBadge(1, 1)
        badge1.description = "blah 1"
        badge1.helpUrl = "http://www.greatlink.com"
        badge1.iconClass = "fas fa-adjust"
        skillsService.createBadge(badge1)
        skills[0..2].each {
            skillsService.assignSkillToBadge(p1.projectId, badge1.badgeId, it.skillId)
        }
        badge1.enabled = true
        skillsService.createBadge(badge1)
        skills[0..2].each {
            skillsService.removeSkillFromBadge([projectId: p1.projectId, badgeId: badge1.badgeId, skillId: it.skillId])
        }

        when:
        def projToCopy = createProject(2)
        skillsService.copyProject(p1.projectId, projToCopy)
        def copied1 = skillsService.getBadge([projectId: projToCopy.projectId, badgeId: badge1.badgeId])

        then:
        copied1.description == badge1.description
        copied1.badgeId == badge1.badgeId
        copied1.name == badge1.name
        copied1.helpUrl == badge1.helpUrl
        copied1.numSkills == 0
        copied1.totalPoints == 0
        copied1.enabled == "false"
        copied1.iconClass == badge1.iconClass
        !copied1.startDate
        !copied1.endDate
    }

    def "validate dependencies were copied"() {
        def p1 = createProject(1)
        skillsService.createProject(p1)

        def p1subj1 = createSubject(1, 1)
        def p1Skills = createSkills(10, 1, 1, 100)
        def group1 = createSkillsGroup(1, 1, 22)
        def group2 = createSkillsGroup(1, 1, 33)
        skillsService.createSubject(p1subj1)
        skillsService.createSkills([p1Skills[0..3], group1, group2].flatten())
        p1Skills[4..9].each {
            skillsService.assignSkillToSkillsGroup(group1.skillId, it)
        }

        def p1subj2 = createSubject(1, 2)
        def p1SkillsSubj2 = createSkills(5, 1, 2, 22)
        def group3 = createSkillsGroup(1, 2, 33)
        skillsService.createSubject(p1subj2)
        skillsService.createSkills([p1SkillsSubj2[0..2], group3].flatten())
        p1SkillsSubj2[3..4].each {
            skillsService.assignSkillToSkillsGroup(group3.skillId, it)
        }

        skillsService.assignDependency([projectId: p1.projectId, skillId: p1Skills.get(0).skillId, dependentSkillId: p1Skills.get(2).skillId])
        skillsService.assignDependency([projectId: p1.projectId, skillId: p1Skills.get(2).skillId, dependentSkillId: p1Skills.get(5).skillId])
        skillsService.assignDependency([projectId: p1.projectId, skillId: p1Skills.get(3).skillId, dependentSkillId: p1SkillsSubj2.get(0).skillId])
        skillsService.assignDependency([projectId: p1.projectId, skillId: p1Skills.get(3).skillId, dependentSkillId: p1SkillsSubj2.get(4).skillId])

        when:
        def projToCopy = createProject(2)
        skillsService.copyProject(p1.projectId, projToCopy)
        def copiedDeps = skillsService.getDependencyGraph(projToCopy.projectId)
        then:
        validateGraph(copiedDeps, [
                new Edge(from: p1Skills[0].skillId, to: p1Skills[2].skillId),
                new Edge(from: p1Skills[2].skillId, to: p1Skills[5].skillId),
                new Edge(from: p1Skills[3].skillId, to: p1SkillsSubj2[0].skillId),
                new Edge(from: p1Skills[3].skillId, to: p1SkillsSubj2[4].skillId),
        ])
    }

    def "validate cross-project dependencies are NOT copied"() {
        def proj1 = SkillsFactory.createProject(1)
        def proj1_subj = SkillsFactory.createSubject(1, 1)
        List<Map> proj1_skills = SkillsFactory.createSkills(3, 1, 1)
        proj1_skills.each {
            it.pointIncrement = 40
        }

        def proj2 = SkillsFactory.createProject(2)
        def proj2_subj = SkillsFactory.createSubject(2, 2)
        List<Map> proj2_skills = SkillsFactory.createSkills(2, 2, 2)
        proj2_skills.each {
            it.pointIncrement = 50
        }

        skillsService.createProject(proj1)
        skillsService.createSubject(proj1_subj)
        skillsService.createSkills(proj1_skills)

        skillsService.createProject(proj2)
        skillsService.createSubject(proj2_subj)
        skillsService.createSkills(proj2_skills)

        skillsService.shareSkill(proj1.projectId, proj1_skills.get(0).skillId, proj2.projectId)
        skillsService.assignDependency([projectId         : proj2.projectId, skillId: proj2_skills.get(0).skillId,
                                        dependentProjectId: proj1.projectId, dependentSkillId: proj1_skills.get(0).skillId,])

        when:
        // proj2 copy
        def proj2ToCopy = createProject(3)
        skillsService.copyProject(proj2.projectId, proj2ToCopy)

        // proj1 copy
        def proj1ToCopy = createProject(4)
        skillsService.copyProject(proj1.projectId, proj1ToCopy)

        def copiedDeps = skillsService.getDependencyGraph(proj2ToCopy.projectId)
        def copiedDeps1 = skillsService.getDependencyGraph(proj1ToCopy.projectId)
        def originalDeps = skillsService.getDependencyGraph(proj2.projectId)
        then:
        !copiedDeps.edges
        !copiedDeps.nodes

        !copiedDeps1.edges
        !copiedDeps1.nodes

        validateGraph(originalDeps, [
                new Edge(from: proj2_skills.get(0).skillId, to: proj1_skills.get(0).skillId),
        ])
    }

    def "projects copied by a root user must be pinned to the user"() {
        SkillsService rootUser = createRootSkillService()
        def p1 = createProject(1)
        rootUser.createProject(p1)
        // UI pins - so have to simulate
        rootUser.pinProject(p1.projectId)

        when:
        def projToCopy = createProject(2)
        rootUser.copyProject(p1.projectId, projToCopy)

        def projects = rootUser.getProjects()
        then:
        projects.projectId == [p1.projectId, projToCopy.projectId]
    }

    static class Edge {
        String from
        String to
    }

    private void validateGraph(def graph, List<Edge> expectedGraphRel) {
        def skill0IdMap0_before = graph.nodes.collectEntries { [it.skillId, it.id] }
        assert graph.edges.collect { "${it.fromId}->${it.toId}" }.sort() == expectedGraphRel.collect {
            "${skill0IdMap0_before.get(it.from)}->${skill0IdMap0_before.get(it.to)}"
        }.sort()
    }
}
