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
package skills.services.admin.moveSkills

import callStack.profiler.Profile
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import skills.controller.exceptions.ErrorCode
import skills.controller.exceptions.SkillException
import skills.controller.request.model.SkillsActionRequest
import skills.services.RuleSetDefGraphService
import skills.services.UserAchievementsAndPointsManagement
import skills.services.admin.DisplayOrderService
import skills.services.admin.SkillCatalogFinalizationService
import skills.services.admin.BatchOperationsTransactionalAccessor
import skills.storage.accessors.SkillDefAccessor
import skills.storage.model.SkillDef
import skills.storage.model.SkillRelDef
import skills.storage.repos.SkillDefRepo
import skills.storage.repos.SkillRelDefRepo
import skills.storage.repos.UserAchievedLevelRepo
import skills.storage.repos.UserPointsRepo

@Service
@Slf4j
class SkillsMoveService {

    @Autowired
    SkillRelDefRepo skillRelDefRepo

    @Autowired
    SkillDefRepo skillDefRepo

    @Autowired
    RuleSetDefGraphService ruleSetDefGraphService

    @Autowired
    SkillDefAccessor skillDefAccessor

    @Autowired
    BatchOperationsTransactionalAccessor batchOperationsTransactionalAccessor

    @Autowired
    SkillCatalogFinalizationService skillCatalogFinalizationService

    @Autowired
    UserPointsRepo userPointsRepo

    @Autowired
    UserAchievementsAndPointsManagement userAchievementsAndPointsManagement

    @Autowired
    UserAchievedLevelRepo userAchievedLevelRepo

    @Autowired
    DisplayOrderService displayOrderService

    @Transactional
    @Profile
    void moveSkills(String projectId, SkillsActionRequest skillReuseRequest) {
        skillCatalogFinalizationService.validateNotInFinalizationState(projectId, "Cannot move skills while finalization is running")
        skillCatalogFinalizationService.validateFinalizationIsNotPending(projectId, "Cannot move skills while finalization is pending")

        // Please note that order is important as achievements calculations relied on points being updated first
        SkillDef origParentSkill = moveDefinitionToDestParent(projectId, skillReuseRequest)
        SkillDef destSubj = updateDestDefinitionPoints(projectId, skillReuseRequest)
        updateOrigDefinitionPoints(projectId, origParentSkill, destSubj)

        SkillDef origSubj = origParentSkill.type == SkillDef.ContainerType.SkillsGroup ? ruleSetDefGraphService.getParentSkill(origParentSkill.id) : origParentSkill

        // points and achievements do not need to be updated if skill remains within the same subject
        if (origSubj.skillId != destSubj.skillId) {
            updateUserPointsInOrigSubject(projectId, origSubj)
            updateUserPointsInDestSubject(projectId, destSubj)

            updateOrigSubjectUserLevelAchievements(origSubj)
            updateDestSubjectUserLevelAchievements(destSubj)
        }

        handleEmptyOrigGroup(origParentSkill)
        handleDestGroupAchievements(projectId, skillReuseRequest)
    }

    @Profile
    private void handleDestGroupAchievements(String projectId, SkillsActionRequest skillReuseRequest) {
        if (skillReuseRequest.groupId) {
            SkillDef group = skillDefAccessor.getSkillDef(projectId, skillReuseRequest.groupId)
            batchOperationsTransactionalAccessor.identifyAndAddGroupAchievements([group])
        }
    }

    @Profile
    private handleEmptyOrigGroup(SkillDef origParentSkill) {
        if (origParentSkill.type == SkillDef.ContainerType.SkillsGroup) {
            Long numChildren = skillRelDefRepo.countChildren(origParentSkill.projectId, origParentSkill.skillId, [SkillRelDef.RelationshipType.SkillsGroupRequirement])
            if (numChildren == 0) {
                userAchievedLevelRepo.deleteByProjectIdAndSkillId(origParentSkill.projectId, origParentSkill.skillId)
            }
        }
    }

    @Profile
    private void updateDestSubjectUserLevelAchievements(SkillDef destSubj) {
        userAchievementsAndPointsManagement.removeSubjectLevelAchievementsIfUsersDoNotQualify(destSubj)
        batchOperationsTransactionalAccessor.identifyAndAddSubjectLevelAchievements(destSubj.projectId, destSubj.skillId)
    }

    @Profile
    private void updateOrigSubjectUserLevelAchievements(SkillDef origSubj) {
        userAchievementsAndPointsManagement.removeSubjectLevelAchievementsIfUsersDoNotQualify(origSubj)
        batchOperationsTransactionalAccessor.identifyAndAddSubjectLevelAchievements(origSubj.projectId, origSubj.skillId)
    }

    @Profile
    private void updateUserPointsInDestSubject(String projectId, SkillDef destSubj) {
        batchOperationsTransactionalAccessor.createSubjectUserPointsForTheNewUsers(projectId, destSubj.skillId)
        batchOperationsTransactionalAccessor.updateUserPointsForSubject(projectId, destSubj.skillId)
    }

    @Profile
    private void updateUserPointsInOrigSubject(String projectId, SkillDef origSubj) {
        removeSubjectUserPointsForNonExistentSkillDef(projectId, origSubj)
        batchOperationsTransactionalAccessor.updateUserPointsForSubject(projectId, origSubj.skillId)
    }

    @Profile
    private removeSubjectUserPointsForNonExistentSkillDef(String projectId, SkillDef origSubj) {
        userPointsRepo.removeSubjectUserPointsForNonExistentSkillDef(projectId, origSubj.skillId)
    }

    @Profile
    private SkillDef moveDefinitionToDestParent(String projectId, SkillsActionRequest skillReuseRequest) {
        String destParentSkillId = skillReuseRequest.groupId ?: skillReuseRequest.subjectId
        boolean isGroupDest = skillReuseRequest.groupId

        SkillDef origParentSkill
        skillReuseRequest.skillIds.each { String skillId ->
            SkillDef skillToMove = skillDefAccessor.getSkillDef(projectId, skillId)
            // make display order very high so the skills are added at the end of the display order
            // please note that display order will be reset at the end
            skillToMove.displayOrder = skillToMove.displayOrder + 10000 // no way someone has 10k skills
            skillToMove.groupId = isGroupDest ? skillReuseRequest.groupId : null
            skillDefRepo.save(skillToMove)

            SkillDef parentSkill = ruleSetDefGraphService.getParentSkill(skillToMove.id)
            if (origParentSkill && origParentSkill.skillId != parentSkill.skillId) {
                throw new SkillException("All moved skills must come from the same parent. But 2 parents were found: [${origParentSkill.skillId}] and [${parentSkill.skillId}] ", projectId, skillId, ErrorCode.BadParam)
            }
            if (parentSkill.skillId == destParentSkillId) {
                throw new SkillException("Skill with id [$skillId] already exists under [$destParentSkillId]", projectId, skillId, ErrorCode.BadParam)
            }
            origParentSkill = parentSkill

            SkillDef subject = (parentSkill.type == SkillDef.ContainerType.SkillsGroup) ? ruleSetDefGraphService.getParentSkill(parentSkill.id) : parentSkill

            boolean isGroupOrig = parentSkill.type == SkillDef.ContainerType.SkillsGroup
            boolean stayingInTheSameSubject = skillReuseRequest.subjectId == subject.skillId
            boolean moveSkillBetweenGroupsInTheSameSubject = stayingInTheSameSubject && isGroupOrig && isGroupDest

            // handle original
            if (isGroupOrig) {
                ruleSetDefGraphService.removeGraphRelationship(projectId, parentSkill.skillId, SkillDef.ContainerType.SkillsGroup, projectId, skillId, SkillRelDef.RelationshipType.SkillsGroupRequirement)
                if (!moveSkillBetweenGroupsInTheSameSubject) {
                    ruleSetDefGraphService.removeGraphRelationship(projectId, subject.skillId, SkillDef.ContainerType.Subject, projectId, skillId, SkillRelDef.RelationshipType.GroupSkillToSubject)
                }
                if (parentSkill.numSkillsRequired > 0) {
                    Long numSkillsInGroup = ruleSetDefGraphService.countChildrenSkills(projectId, parentSkill.skillId, [SkillRelDef.RelationshipType.SkillsGroupRequirement])
                    if (numSkillsInGroup <= parentSkill.numSkillsRequired) {
                        // -1 is the default = ALL
                        parentSkill.numSkillsRequired = -1
                    }
                }
            } else {
                ruleSetDefGraphService.removeGraphRelationship(projectId, parentSkill.skillId, SkillDef.ContainerType.Subject, projectId, skillId, SkillRelDef.RelationshipType.RuleSetDefinition)
            }

            // handle destination
            if (isGroupDest) {
                ruleSetDefGraphService.assignGraphRelationship(projectId, skillReuseRequest.groupId, SkillDef.ContainerType.SkillsGroup, projectId, skillId, SkillRelDef.RelationshipType.SkillsGroupRequirement)
                if (!moveSkillBetweenGroupsInTheSameSubject) {
                    ruleSetDefGraphService.assignGraphRelationship(projectId, skillReuseRequest.subjectId, SkillDef.ContainerType.Subject, projectId, skillId, SkillRelDef.RelationshipType.GroupSkillToSubject)
                }
            } else {
                ruleSetDefGraphService.assignGraphRelationship(projectId, skillReuseRequest.subjectId, SkillDef.ContainerType.Subject, projectId, skillId, SkillRelDef.RelationshipType.RuleSetDefinition)
            }
        }

        resetDisplayOrder(origParentSkill, destParentSkillId)
        return origParentSkill
    }

    private void resetDisplayOrder(SkillDef origParentSkill, String parentSkillId) {
        List<SkillDef> siblingsOrig = ruleSetDefGraphService.getChildrenSkills(origParentSkill, [SkillRelDef.RelationshipType.RuleSetDefinition, SkillRelDef.RelationshipType.SkillsGroupRequirement])
        displayOrderService.resetDisplayOrder(siblingsOrig)
        SkillDef desParent = skillDefAccessor.getSkillDef(origParentSkill.projectId, parentSkillId, [SkillDef.ContainerType.Subject, SkillDef.ContainerType.SkillsGroup])
        List<SkillDef> siblingsDest = ruleSetDefGraphService.getChildrenSkills(desParent, [SkillRelDef.RelationshipType.RuleSetDefinition, SkillRelDef.RelationshipType.SkillsGroupRequirement])
        displayOrderService.resetDisplayOrder(siblingsDest)
    }

    @Profile
    private void updateOrigDefinitionPoints(String projectId, SkillDef origParentSkill, SkillDef destSubj) {
        // update points for the origination group and/or subject
        // optimization - handle the case where skill was moved from a group to its parent subject
        if (destSubj.skillId != origParentSkill.skillId) {
            if (origParentSkill.type == SkillDef.ContainerType.SkillsGroup) {
                batchOperationsTransactionalAccessor.updateGroupTotalPoints(projectId, origParentSkill.skillId)
                SkillDef subject = ruleSetDefGraphService.getParentSkill(origParentSkill.id)
                batchOperationsTransactionalAccessor.updateSubjectTotalPoints(projectId, subject.skillId)
            } else {
                batchOperationsTransactionalAccessor.updateSubjectTotalPoints(projectId, origParentSkill.skillId)
            }
        }
    }

    @Profile
    private SkillDef updateDestDefinitionPoints(String projectId, SkillsActionRequest skillReuseRequest) {
        boolean isGroupDest = skillReuseRequest.groupId

        SkillDef destSubj
        if (isGroupDest) {
            SkillDef group = skillDefAccessor.getSkillDef(projectId, skillReuseRequest.groupId)
            batchOperationsTransactionalAccessor.updateGroupTotalPoints(projectId, skillReuseRequest.groupId)
            destSubj = ruleSetDefGraphService.getParentSkill(group.id)
            batchOperationsTransactionalAccessor.updateSubjectTotalPoints(projectId, destSubj.skillId)
        } else {
            destSubj = skillDefAccessor.getSkillDef(projectId, skillReuseRequest.subjectId, [SkillDef.ContainerType.Subject])
            batchOperationsTransactionalAccessor.updateSubjectTotalPoints(projectId, destSubj.skillId)
        }
        return destSubj
    }

}
