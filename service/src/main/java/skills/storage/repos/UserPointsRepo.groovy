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
package skills.storage.repos

import groovy.transform.CompileStatic
import org.apache.commons.lang3.ObjectUtils
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.lang.Nullable
import skills.controller.result.model.ProjectUser
import skills.storage.model.SkillRelDef
import skills.storage.model.DayCountItem
import skills.storage.model.UserPoints

import java.time.LocalDateTime

@CompileStatic
interface UserPointsRepo extends CrudRepository<UserPoints, Integer> {

    @Nullable
    UserPoints findByProjectIdAndUserIdAndSkillId(String projectId, String userId, @Nullable String skillId)

    @Modifying
    @Query(value = '''INSERT INTO user_points(user_id, project_id, skill_id, skill_ref_id, points)
            SELECT up.user_id, toDef.project_id, toDef.skill_id, toDef.id, up.points
            FROM user_points up, skill_definition toDef
            WHERE
                  toDef.project_id = :toProjectId and 
                  toDef.copied_from_skill_ref = up.skill_ref_id and
                  up.skill_ref_id in (:fromSkillRefIds)
                  and not exists (
                    select 1 from user_points innerUP
                    where
                      toDef.project_id = innerUP.project_id
                      and up.user_id = innerUP.user_id
                      and toDef.skill_id = innerUP.skill_id
                  )
            ''', nativeQuery = true)
    void copySkillUserPointsToTheImportedProjects(@Param('toProjectId') String toProjectId, @Param('fromSkillRefIds') List<Integer> fromSkillRefIds)

    @Modifying
    @Query(value = '''INSERT INTO user_points(user_id, points, project_id)
            SELECT up.user_id, sum(points) as points, max(up.project_id) as project_id
            FROM user_points up, skill_definition sd
            WHERE
                    up.project_id = :toProjectId and
                    sd.project_id = :toProjectId and sd.id = up.skill_ref_id and sd.type = 'Skill'
            and not exists (select 1 from user_points innerUP where up.project_id = innerUP.project_id and up.user_id = innerUP.user_id and innerUP.skill_id is null)
            group by up.user_id;
            ''', nativeQuery = true)
    void createProjectUserPointsForTheNewUsers(@Param('toProjectId') String toProjectId)


    @Modifying
    @Query(value = '''INSERT INTO user_points(user_id, points, project_id, skill_id, skill_ref_id)
        SELECT up.user_id, sum(points) as points, max(up.project_id) as project_id, max(subject.skill_id) as skill_id, max(subject.id) as skill_ref_id
        FROM user_points up, skill_definition subject, skill_relationship_definition srd, skill_definition sd
        WHERE
          up.project_id = :toProjectId
          and subject.project_id = :toProjectId and subject.skill_id = :toSubjectId
          and subject.id = srd.parent_ref_id and sd.id = srd.child_ref_id and srd.type in ('RuleSetDefinition', 'GroupSkillToSubject')
          and sd.id = up.skill_ref_id
          and not exists (
            select 1 from user_points innerUP
            where
              up.project_id = innerUP.project_id
              and innerUP.skill_id = :toSubjectId
              and up.user_id = innerUP.user_id
          )
        group by up.user_id;
            ''', nativeQuery = true)
    void createSubjectUserPointsForTheNewUsers(@Param('toProjectId') String toProjectId, @Param('toSubjectId') String toSubjectId)

    @Nullable
    @Query('''SELECT p.points as points from UserPoints p where p.projectId=?1 and p.userId=?2 and p.skillId is null''')
    Integer findPointsByProjectIdAndUserId(String projectId, String userId)

    @Nullable
    @Query('''SELECT p.points as points from UserPoints p where p.projectId=?1 and p.userId=?2 and p.skillId=?3''')
    Integer findPointsByProjectIdAndUserIdAndSkillId(String projectId, String userId, String skillId)


    static interface RankedUserRes {
        String getUserId()
        String getUserIdForDisplay()
        String getUserFirstName()
        String getUserLastName()
        String getUserNickname()
        Integer getPoints()
        LocalDateTime getUserFirstSeenTimestamp()
    }


    @Query('''SELECT 
                    p.userId as userId, 
                    p.points as points,
                    uAttrs.userIdForDisplay as userIdForDisplay,
                    uAttrs.firstName as userFirstName,
                    uAttrs.lastName as userLastName,
                    uAttrs.nickname as userNickname,
                    uAttrs.created as userFirstSeenTimestamp
                from UserPoints p, UserAttrs uAttrs
                where
                    p.userId = uAttrs.userId and
                    p.projectId=?1 and 
                    p.skillId is null and 
                    p.userId not in ?2 and
                    p.userId not in 
                        (select u.userId from Setting s, User u where 
                            s.userRefId=u.id and 
                            s.settingGroup='user.prefs' and
                            s.setting='rank_and_leaderboard_optOut' and
                            s.value='true' and 
                            s.projectId is null)
            ''')
    List<RankedUserRes> findUsersForLeaderboard(String projectId, List<String> excludeUserIds, Pageable pageable)

    @Query('''SELECT 
                    p.userId as userId, 
                    p.points as points,
                    uAttrs.userIdForDisplay as userIdForDisplay,
                    uAttrs.firstName as userFirstName,
                    uAttrs.lastName as userLastName,
                    uAttrs.nickname as userNickname,
                    uAttrs.created as userFirstSeenTimestamp
                from UserPoints p, UserAttrs uAttrs
                where
                    p.userId = uAttrs.userId and
                    p.projectId=?1 and 
                    p.skillId=?2 and 
                    p.userId not in ?3 and
                    p.userId not in 
                        (select u.userId from Setting s, User u where 
                            s.userRefId=u.id and 
                            s.settingGroup='user.prefs' and
                            s.setting='rank_and_leaderboard_optOut' and
                            s.value='true' and 
                            s.projectId is null)
            ''')
    List<RankedUserRes> findUsersForLeaderboard(String projectId, String subjectId, List<String> excludeUserIds, Pageable pageable)

    @Query('''SELECT 
                    p.userId as userId, 
                    p.points as points,
                    uAttrs.userIdForDisplay as userIdForDisplay,
                    uAttrs.firstName as userFirstName,
                    uAttrs.lastName as userLastName,
                    uAttrs.nickname as userNickname,
                    uAttrs.created as userFirstSeenTimestamp
                from UserPoints p, UserAttrs uAttrs
                where
                    p.userId = uAttrs.userId and
                    p.projectId=?1 and 
                    p.skillId=?2 and
                    (p.points<?3 OR (p.points=?3 and uAttrs.created>?4))
            ''')
    List<RankedUserRes> findUsersForLeaderboardPointsLessOrEqual(String projectId, String subjectId, Integer points, LocalDateTime usrCreated, Pageable pageable)

    @Query('''SELECT 
                    p.userId as userId, 
                    p.points as points,
                    uAttrs.userIdForDisplay as userIdForDisplay,
                    uAttrs.firstName as userFirstName,
                    uAttrs.lastName as userLastName,
                    uAttrs.nickname as userNickname,
                    uAttrs.created as userFirstSeenTimestamp
                from UserPoints p, UserAttrs uAttrs
                where
                    p.userId = uAttrs.userId and
                    p.projectId=?1 and 
                    p.skillId is null and
                    p.points<=?2 and
                    (p.points<?2 OR (p.points=?2 and uAttrs.created>?3))
            ''')
    List<RankedUserRes> findUsersForLeaderboardPointsLessOrEqual(String projectId, Integer points, LocalDateTime usrCreated, Pageable pageable)

    @Query('''SELECT 
                    p.userId as userId, 
                    p.points as points,
                    uAttrs.userIdForDisplay as userIdForDisplay,
                    uAttrs.firstName as userFirstName,
                    uAttrs.lastName as userLastName,
                    uAttrs.nickname as userNickname,
                    uAttrs.created as userFirstSeenTimestamp
                from UserPoints p, UserAttrs uAttrs
                where
                    p.userId = uAttrs.userId and
                    p.projectId=?1 and 
                    p.skillId=?2 and
                    (p.points>?3 OR (p.points=?3 and uAttrs.created<?4))
            ''')
    List<RankedUserRes> findUsersForLeaderboardPointsMoreOrEqual(String projectId, String subjectId, Integer points, LocalDateTime userCreateDate, Pageable pageable)

    @Query('''SELECT 
                    p.userId as userId, 
                    p.points as points,
                    uAttrs.userIdForDisplay as userIdForDisplay,
                    uAttrs.firstName as userFirstName,
                    uAttrs.lastName as userLastName,
                    uAttrs.nickname as userNickname,
                    uAttrs.created as userFirstSeenTimestamp
                from UserPoints p, UserAttrs uAttrs
                where
                    p.userId = uAttrs.userId and
                    p.projectId=?1 and 
                    p.skillId is null and
                    (p.points>?2 OR (p.points=?2 and uAttrs.created<?3))
            ''')
    List<RankedUserRes>  findUsersForLeaderboardPointsMoreOrEqual(String projectId, Integer points, LocalDateTime createdDate, Pageable pageable)

    @Query('''SELECT DISTINCT(p.userId) from UserPoints p 
                where
                    p.projectId =?1 and
                    lower(p.userId) LIKE %?2%''' )
    List<String> findDistinctUserIdsForProject(String projectId, String userIdQuery, Pageable pageable)

    long countByProjectIdAndSkillId(String projectId, @Nullable String skillId)

    void deleteByProjectIdAndSkillId(String projectId, String skillId)

    @Query('''SELECT count(p) from UserPoints p where 
            p.projectId=?1 and 
            p.skillId=?2 and 
            p.points > ?3 and 
            p.userId not in ?4 and 
            p.userId not in 
                (select u.userId from Setting s, User u where 
                    s.userRefId=u.id and 
                    s.settingGroup='user.prefs' and
                    s.setting='rank_and_leaderboard_optOut' and
                    s.value='true' and 
                    s.projectId is null)''' )
    Integer calculateNumUsersWithLessScore(String projectId, String skillId, int points, List<String> excludeUserIds)

    @Query('''SELECT count(p) from UserPoints p where 
            p.projectId=?1 and 
            p.skillId is null and 
            p.points > ?2 and 
            p.userId not in ?3 and 
            p.userId not in 
                (select u.userId from Setting s, User u where 
                    s.userRefId=u.id and 
                    s.settingGroup='user.prefs' and
                    s.setting='rank_and_leaderboard_optOut' and
                    s.value='true' and 
                    s.projectId is null)''' )
    Integer calculateNumUsersWithLessScore(String projectId, int points, List<String> excludeUserIds)

    @Query('''SELECT count(p) from UserPoints p, UserAttrs ua where 
            p.userId = ua.userId and 
            p.projectId=?1 and 
            p.skillId=?2 and 
            (p.points > ?3 OR (p.points = ?3 and ua.created < ?4))''' )
    Integer calculateNumUsersWithHigherScoreAndIfScoreTheSameThenAfterUserCreateDate(String projectId, String skillId, int points, LocalDateTime created)

    @Query('''SELECT count(p) from UserPoints p, UserAttrs ua where
            p.userId = ua.userId and 
            p.projectId=?1 and 
            p.skillId is null and 
            (p.points > ?2 OR (p.points = ?2 and ua.created < ?3))''')
    Integer calculateNumUsersWithHigherScoreAndIfScoreTheSameThenAfterUserCreateDate(String projectId, int points, LocalDateTime created)

    List<UserPoints> findByProjectIdAndSkillIdAndPointsGreaterThan(String projectId, @Nullable String skillId, int points, Pageable pageable)
    List<UserPoints> findByProjectIdAndSkillIdAndPointsLessThan(String projectId, @Nullable String skillId, int points, Pageable pageable)

    List<UserPoints> findByProjectIdAndUserId(String projectId, String userId)

    static interface SkillRefIdWithPoints {
        Integer getSkillRefId()
        Integer getPoints()
    }

    @Query(value ='''WITH skills AS (
                select case when child.copied_from_skill_ref is not null then child.copied_from_skill_ref else child.id end as id,
                       child.point_increment as point_increment,
                       child.id as originalRefId
                from skill_relationship_definition rel,
                     skill_definition child
                where rel.parent_ref_id = :parentSkillRefId
                  and rel.child_ref_id = child.id
                  and rel.type in :relationshipTypes
                  and child.type = 'Skill'
                  and child.enabled = 'true'
            )
            select skills.originalRefId skillRefId, sum(skills.point_increment) points
            from user_performed_skill up,
                 skills
            where up.user_id =:userId
              and up.skill_ref_id = skills.id
              and DATE_TRUNC('DAY', up.performed_on) = :day
            group by skills.originalRefId''', nativeQuery = true)
    List<SkillRefIdWithPoints> calculatePointsForChildSkillsForADay(@Param("userId") String userId,
                                                        @Param("parentSkillRefId") Integer parentSkillRefId,
                                                        @Param("relationshipTypes") List<String> relationshipTypes,
                                                        @Param("day") Date day)



    @Nullable
    @Query(value ='''select sum(sd.point_increment) points
            from user_performed_skill up,
                 skill_definition sd
            where up.user_id =:userId
              and sd.id = :skillRefId
              and up.skill_ref_id = (case when sd.copied_from_skill_ref is not null then sd.copied_from_skill_ref else sd.id end)
              and DATE_TRUNC('DAY', up.performed_on) = :day 
            group by up.skill_ref_id''', nativeQuery = true)
    Integer calculatePointsForSingleSkillForADay(@Param("userId") String userId,
                                                 @Param("skillRefId") Integer skillRefId,
                                                 @Param("day") Date day)


    /**
     *  NOTE: this is query is identical to the above query the only difference is 'userPoints.day is null', if you change this query you MUST change the one above
     *
     *  the reason for duplication is that when null is provided for the 'day' parameter JPA doesn't properly generate SQL statement, I am guessing the bug is because
     *      *  the parameter is withing left join clause and they didn't handle that properly
     */
    @Query('''select sdChild, userPoints, pd.name, approval
    from SkillDef sdParent, SkillRelDef srd, SkillDef sdChild
    left join UserPoints userPoints on sdChild.projectId = userPoints.projectId and sdChild.skillId = userPoints.skillId and userPoints.userId=?1
    left join ProjDef pd on sdChild.copiedFromProjectId = pd.projectId
    left join SkillApproval approval on sdChild.id = approval.skillRefId and approval.userId=?1 and approval.projectId=?2 and approval.rejectedOn=null and approval.approverUserId=null
      where srd.parent=sdParent.id and  srd.child=sdChild.id and (sdChild.enabled = 'true' or sdChild.type = 'SkillsGroup') and
      sdParent.projectId=?2 and sdParent.skillId=?3 and srd.type in ?4 and sdChild.version<=?5 ''')
    List<Object []> findChildrenAndTheirUserPoints(String userId, String projectId, String skillId, List<SkillRelDef.RelationshipType> types, Integer version)


    /**
     *  NOTE: this is query is identical to the above query the only difference is 'userPoints.day is null', if you change this query you MUST change the one above
     *
     *  the reason for duplication is that when null is provided for the 'day' parameter JPA doesn't properly generate SQL statement, I am guessing the bug is because
     *      *  the parameter is withing left join clause and they didn't handle that properly
     */
    @Query('''select sdChild, userPoints, approval
    from SkillDef sdParent, SkillRelDef srd, SkillDef sdChild
    left join UserPoints userPoints on sdChild.projectId = userPoints.projectId and sdChild.skillId = userPoints.skillId and userPoints.userId=?1
    left join SkillApproval approval on sdChild.id = approval.skillRefId and approval.userId=?1 and approval.rejectedOn=null and approval.approverUserId=null
      where srd.parent=sdParent.id and  srd.child=sdChild.id and sdChild.enabled = 'true' and
      sdParent.projectId is null and sdParent.skillId=?2 and srd.type in ?3 and sdChild.version<=?4''')
    List<Object []> findGlobalChildrenAndTheirUserPoints(String userId, String skillId, List<SkillRelDef.RelationshipType> types, Integer version)

    @Query('''select sdChild.id, achievement.id
    from SkillDef sdParent, SkillRelDef srd, SkillDef sdChild
    left join UserAchievement achievement on sdChild.projectId = achievement.projectId and sdChild.skillId = achievement.skillId and achievement.userId=?1
      where srd.parent=sdParent.id and  srd.child=sdChild.id and
      sdParent.projectId=?2 and sdParent.skillId=?3 and srd.type=?4 and sdChild.version<=?5''')
    List<Object []> findChildrenAndTheirAchievements(String userId, String projectId, String skillId, SkillRelDef.RelationshipType type, Integer version)

    @Query('''select sdParent.id as parentId, sdChild.id as childId, achievement.id as achievementId
    from SkillDef sdParent, SkillRelDef srd, SkillDef sdChild
    left join UserAchievement achievement on sdChild.projectId = achievement.projectId and sdChild.skillId = achievement.skillId and achievement.userId=?1
      where srd.parent=sdParent.id and  srd.child=sdChild.id and
      sdParent.projectId=?2 and srd.type=?3 and sdChild.version<=?4''')
    List<SkillWithChildAndAchievementIndicator> findAllChildrenAndTheirAchievementsForProject(String userId, String projectId, SkillRelDef.RelationshipType type, Integer version)

    @Query('''select sdParent.id as parentId, sdChild.id as childId, achievement.id as achievementId
    from SkillDef sdParent, SkillRelDef srd, SkillDef sdChild
    left join UserAchievement achievement on sdChild.projectId = achievement.projectId and sdChild.skillId = achievement.skillId and achievement.userId=?1
      where srd.parent=sdParent.id and  srd.child=sdChild.id and srd.type=?2 and sdChild.version<=?3''')
    List<SkillWithChildAndAchievementIndicator> findAllChildrenAndTheirAchievementsForGlobal(String userId, SkillRelDef.RelationshipType type, Integer version)

    static interface SkillWithChildAndAchievementIndicator {
        Integer getParentId()
        Integer getChildId()
        Integer getAchievementId()
    }
    @Query(value = '''SELECT COUNT(distinct user_id) from user_points where project_id = ?1 and skill_id is null''', nativeQuery = true)
    Long countDistinctUserIdByProjectId(String projectId)

    @Query(value = '''SELECT COUNT(*)
        FROM (SELECT DISTINCT usattr.user_id 
                FROM user_points usr, user_attrs usattr 
                where usr.user_id = usattr.user_id and 
                    usr.project_id = ?1 and 
                    usr.skill_id is null and 
                    (lower(CONCAT(usattr.first_name, ' ', usattr.last_name, ' (', usattr.user_id_for_display, ')')) like lower(CONCAT('%', ?2, '%')) OR
                     lower(usattr.user_id_for_display) like lower(CONCAT('%', ?2, '%')))) 
                AS temp''',
            nativeQuery = true)
    Long countDistinctUserIdByProjectIdAndUserIdLike(String projectId, String userId)

    @Query(value = '''SELECT COUNT(DISTINCT up.user_id)
        FROM user_points up, user_tags ut
        WHERE up.user_id = ut.user_id
          AND up.project_id = ?1
          AND up.skill_id IS NULL
          AND ut.key = ?2
          AND ut.value = ?3''', nativeQuery = true)
    Long countDistinctUserIdByProjectIdAndUserTag(String projectId, String userTagKey, String userTagValue)

    @Query(value = '''SELECT COUNT(*)
        FROM (SELECT DISTINCT usattr.user_id 
                FROM user_points usr, user_attrs usattr, user_tags ut
                where usr.user_id = usattr.user_id and 
                      usr.user_id = ut.user_id and 
                      ut.key = ?2 and
                      ut.value = ?3 and
                      usr.project_id = ?1 and 
                      usr.skill_id is null and 
                      (lower(CONCAT(usattr.first_name, ' ', usattr.last_name, ' (', usattr.user_id_for_display, ')')) like lower(CONCAT('%', ?4, '%')) OR
                       lower(usattr.user_id_for_display) like lower(CONCAT('%', ?4, '%')))) 
                AS temp''',
            nativeQuery = true)
    Long countDistinctUserIdByProjectIdAndUserTagAndUserIdLike(String projectId, String userTagKey, String userTagValue, String userId)

    @Query(value = '''SELECT 
                up.user_id as userId, 
                max(upa.performedOn) as lastUpdated, 
                sum(up.points) as totalPoints,
                max(ua.first_name) as firstName,
                max(ua.last_name) as lastName,
                max(ua.dn) as dn,
                max(ua.email) as email,
                max(ua.user_id_for_display) as userIdForDisplay,
                case when max(uAchievement.level) is not null then max(uAchievement.level) else 0 end as userMaxLevel, 
                max(ut.value) as userTag
            FROM user_points up
            LEFT JOIN (
                SELECT upa.user_id, 
                max(upa.performed_on) AS performedOn 
                FROM user_performed_skill upa 
                WHERE upa.skill_ref_id in (
                    select case when copied_from_skill_ref is not null then copied_from_skill_ref else id end as id from skill_definition where type = 'Skill' and project_id = ?1 and enabled = 'true'
                )
                GROUP BY upa.user_id
            ) upa ON upa.user_id = up.user_id
            LEFT JOIN (
                SELECT uAchievement.user_id, max(uAchievement.level) as level
                FROM user_achievement uAchievement 
                WHERE uAchievement.project_id = ?1
                    and uAchievement.skill_id is null
                    and uAchievement.level is not null
                GROUP BY uAchievement.user_id
            ) uAchievement ON uAchievement.user_id = up.user_id
            JOIN user_attrs ua ON ua.user_id=up.user_id
            LEFT JOIN (SELECT ut.user_id, max(ut.value) AS value FROM user_tags ut WHERE ut.key = ?2 group by ut.user_id) ut ON ut.user_id=ua.user_id
            WHERE 
                up.project_id=?1 and 
                (lower(CONCAT(ua.first_name, ' ', ua.last_name, ' (',  ua.user_id_for_display, ')')) like lower(CONCAT(\'%\', ?3, \'%\'))  OR
                 lower(ua.user_id_for_display) like lower(CONCAT('%', ?3, '%'))
                ) and 
                up.skill_id is null 
            GROUP BY up.user_id''', nativeQuery = true)
    List<ProjectUser> findDistinctProjectUsersAndUserIdLike(String projectId, String usersTableAdditionalUserTagKey, String query, Pageable pageable)

    @Query(value='''SELECT COUNT(*)
        FROM (SELECT DISTINCT up.user_id from user_points up where up.project_id=?1 and up.skill_id in (?2)) AS temp''',
            nativeQuery = true)
    Long countDistinctUserIdByProjectIdAndSkillIdIn(String projectId, List<String> skillIds)

    @Query(value= '''
        WITH subj_skills AS (
            select child.id as id
            from skill_definition parent,
                 skill_relationship_definition rel,
                 skill_definition child
            where parent.project_id = :projectId
              and parent.skill_id = :subjectId
              and rel.parent_ref_id = parent.id
              and rel.child_ref_id = child.id
              and rel.type in ('RuleSetDefinition', 'GroupSkillToSubject')
              and child.type = 'Skill'
              and child.enabled = 'true'
        )
        SELECT COUNT(*)
        FROM (
            SELECT DISTINCT up.user_id 
            from user_points up, user_attrs usattr 
            where 
                up.user_id = usattr.user_id and
                up.skill_ref_id in (select id from subj_skills) and 
                (lower(CONCAT(usattr.first_name, ' ', usattr.last_name, ' (', usattr.user_id_for_display, ')')) like lower(CONCAT('%', :userId, '%')) OR
                 lower(usattr.user_id_for_display) like lower(CONCAT('%', :userId, '%')))
        ) AS temp
    ''', nativeQuery = true)
    Long countDistinctUsersByProjectIdAndSubjectIdAndUserIdLike(@Param("projectId") String projectId, @Param("subjectId") String subjectId, @Param("userId") String userId)

    @Query(value= '''
        WITH skills AS (
            select child.id as id
            from skill_definition parent,
                 skill_relationship_definition rel,
                 skill_definition child
            where parent.project_id = :projectId
              and parent.skill_id = :subjectId
              and rel.parent_ref_id = parent.id
              and rel.child_ref_id = child.id
              and rel.type in ('RuleSetDefinition', 'GroupSkillToSubject')
              and child.type = 'Skill'
              and child.enabled = 'true'
        )
        SELECT COUNT(DISTINCT up.user_id) from user_points up where up.skill_ref_id in (select id from skills);
    ''', nativeQuery = true)
    Long countDistinctUsersByProjectIdAndSubjectId(@Param("projectId") String projectId, @Param("subjectId") String subjectId)

    @Query(value='''SELECT COUNT(*)
        FROM (SELECT DISTINCT up.user_id 
            from user_points up, user_attrs usattr 
            where 
                up.user_id = usattr.user_id and
                up.project_id=?1 and 
                up.skill_id in (?2) and 
                (lower(CONCAT(usattr.first_name, ' ', usattr.last_name, ' (', usattr.user_id_for_display, ')')) like lower(CONCAT('%', ?3, '%')) OR
                 lower(usattr.user_id_for_display) like lower(CONCAT('%', ?3, '%')))) 
            AS temp''',
            nativeQuery = true)
    Long countDistinctUserIdByProjectIdAndSkillIdInAndUserIdLike(String projectId, List<String> skillIds, String query)

    @Query(value = '''SELECT 
                up.user_id as userId, 
                max(upa.performedOn) as lastUpdated, 
                sum(up.points) as totalPoints,
                max(ua.first_name) as firstName,
                max(ua.last_name) as lastName,
                max(ua.dn) as dn,
                max(ua.email) as email,
                max(ua.user_id_for_display) as userIdForDisplay,
                max(ut.value) as userTag
            FROM user_points up
            LEFT JOIN (
                SELECT upa.user_id, 
                max(upa.performed_on) AS performedOn 
                FROM user_performed_skill upa 
                WHERE upa.skill_ref_id in (
                    select case when copied_from_skill_ref is not null then copied_from_skill_ref else id end as id from skill_definition sd where type = 'Skill' and sd.project_id = ?1 and sd.skill_id in (?3)
                )
                GROUP BY upa.user_id
            ) upa ON upa.user_id = up.user_id
            JOIN user_attrs ua ON ua.user_id=up.user_id
            LEFT JOIN (SELECT ut.user_id, max(ut.value) AS value FROM user_tags ut WHERE ut.key = ?2 group by ut.user_id) ut ON ut.user_id=ua.user_id
            WHERE 
                up.project_id=?1 and 
                up.skill_id in (?3) and 
                (lower(CONCAT(ua.first_name, ' ', ua.last_name, ' (',  ua.user_id_for_display, ')')) like lower(CONCAT('%', ?4, '%'))  OR
                 lower(ua.user_id_for_display) like lower(CONCAT('%', ?4, '%'))
                ) 
            GROUP BY up.user_id''', nativeQuery = true)
    List<ProjectUser> findDistinctProjectUsersByProjectIdAndSkillIdInAndUserIdLike(String projectId, String usersTableAdditionalUserTagKey, List<String> skillIds, String userId, Pageable pageable)

    @Query(value = '''SELECT 
                up.user_id as userId, 
                max(upa.performedOn) as lastUpdated, 
                sum(up.points) as totalPoints,
                max(ua.first_name) as firstName,
                max(ua.last_name) as lastName,
                max(ua.dn) as dn,
                max(ua.email) as email,
                max(ua.user_id_for_display) as userIdForDisplay,
                max(utCol.utColValue) as userTag 
            FROM user_points up
            LEFT JOIN (
                SELECT upa.user_id, 
                max(upa.performed_on) AS performedOn 
                FROM user_performed_skill upa 
                WHERE upa.skill_ref_id in (
                    select case when copied_from_skill_ref is not null then copied_from_skill_ref else id end as id from skill_definition where type = 'Skill' and project_id = ?1 and enabled = 'true'
                )
                GROUP BY upa.user_id
            ) upa ON upa.user_id = up.user_id
            JOIN user_attrs ua ON ua.user_id=up.user_id
            JOIN user_tags ut ON ut.user_id=up.user_id
            LEFT JOIN (SELECT utCol.user_id, max(utCol.value) AS utColValue FROM user_tags utCol WHERE utCol.key = ?2 group by utCol.user_id) utCol ON utCol.user_id=ua.user_id
            WHERE 
                up.project_id=?1 and 
                ut.key = ?3 and
                ut.value = ?4 and
                (lower(CONCAT(ua.first_name, ' ', ua.last_name, ' (',  ua.user_id_for_display, ')')) like lower(CONCAT(\'%\', ?5, \'%\'))  OR
                 lower(ua.user_id_for_display) like lower(CONCAT('%', ?5, '%'))
                ) and 
                up.skill_id is null 
            GROUP BY up.user_id''', nativeQuery = true)
    List<ProjectUser> findDistinctProjectUsersByProjectIdAndUserTagAndUserIdLike(String projectId, String usersTableAdditionalUserTagKey, String userTagKey, String userTagValue, String userId, Pageable pageable)

    @Nullable
    @Query(value= '''
         WITH subj_skills AS (
            select child.id as id
            from skill_definition parent,
                 skill_relationship_definition rel,
                 skill_definition child
            where parent.project_id = :projectId
              and parent.skill_id = :subjectId
              and rel.parent_ref_id = parent.id
              and rel.child_ref_id = child.id
              and rel.type in ('RuleSetDefinition', 'GroupSkillToSubject')
              and child.type = 'Skill'
              and child.enabled = 'true'
        )
        SELECT 
            up.user_id as userId, 
            max(upa.performedOn) as lastUpdated, 
            sum(up.points) as totalPoints,
            max(ua.first_name) as firstName,
            max(ua.last_name) as lastName,
            max(ua.dn) as dn,
            max(ua.email) as email,
            max(ua.user_id_for_display) as userIdForDisplay,
            case when max(uAchievement.level) is not null then max(uAchievement.level) else 0 end as userMaxLevel, 
            max(ut.value) as userTag 
        FROM user_points up
        LEFT JOIN (
            SELECT upa.user_id, 
            max(upa.performed_on) AS performedOn 
            FROM user_performed_skill upa 
            WHERE upa.skill_ref_id in (
                select case when copied_from_skill_ref is not null then copied_from_skill_ref else id end as id from skill_definition where type = 'Skill' and project_id = :projectId and exists (select 1 from subj_skills s_s where s_s.id = id)
            )
            GROUP BY upa.user_id
        ) upa ON upa.user_id = up.user_id
        LEFT JOIN (
            SELECT uAchievement.user_id, max(uAchievement.level) as level
            FROM user_achievement uAchievement 
            WHERE uAchievement.project_id = :projectId
                and uAchievement.skill_id = :subjectId
                and uAchievement.level is not null
            GROUP BY uAchievement.user_id
        ) uAchievement ON uAchievement.user_id = up.user_id    
        JOIN user_attrs ua ON ua.user_id=up.user_id
        LEFT JOIN (SELECT ut.user_id, max(ut.value) AS value FROM user_tags ut WHERE ut.key = :usersTableAdditionalUserTagKey group by ut.user_id) ut ON ut.user_id=ua.user_id
        WHERE 
            up.skill_ref_id in (select s_s.id from subj_skills s_s) and 
            (lower(CONCAT(ua.first_name, ' ', ua.last_name, ' (',  ua.user_id_for_display, ')')) like lower(CONCAT('%', :userId, '%'))  OR
             lower(ua.user_id_for_display) like lower(CONCAT('%', :userId, '%'))
            ) 
        GROUP BY up.user_id
    ''', nativeQuery = true)
    List<ProjectUser> findDistinctProjectUsersByProjectIdAndSubjectIdAndUserIdLike(@Param("projectId") String projectId,
                                                                                   @Param("usersTableAdditionalUserTagKey") String usersTableAdditionalUserTagKey,
                                                                                   @Param("subjectId") String subjectId,
                                                                                   @Param("userId") String userId,
                                                                                   Pageable pageable)

    @Nullable
    @Query('SELECT up.points from UserPoints up where up.projectId=?1 and up.userId=?2 and up.skillRefId=?3')
    Integer getPointsByProjectIdAndUserIdAndSkillRefId(String projectId, String userId, @Nullable Integer skillRefId)

    @Nullable
    @Query('SELECT up.points from UserPoints up where up.projectId=?1 and up.userId=?2 and up.skillRefId is null')
    Integer getPointsByProjectIdAndUserId(String projectId, String userId)

    @Nullable
    @Query(value ='''WITH skills AS (
                select case when child.copied_from_skill_ref is not null then child.copied_from_skill_ref else child.id end as id,
                       child.point_increment as point_increment
                from skill_relationship_definition rel,
                     skill_definition child
                where rel.parent_ref_id = :parentSkillRefId
                  and rel.child_ref_id = child.id
                  and rel.type in ('RuleSetDefinition', 'SkillsGroupRequirement', 'GroupSkillToSubject')
                  and child.type = 'Skill'
                  and child.enabled = 'true'
            )
            select sum(skills.point_increment)
            from user_performed_skill up,
                 skills
            where up.user_id =:userId
              and up.skill_ref_id = skills.id
              and DATE_TRUNC('DAY', up.performed_on) = :day ''', nativeQuery = true)
    Integer getParentsPointsForAGivenDay(@Param("userId") String userId, @Param("parentSkillRefId") Integer parentSkillRefId, @Param("day") Date day)

    @Modifying
    @Query(value = '''UPDATE 
                        user_points up 
                        SET points = points+?4 
                    WHERE 
                        up.project_id=?1 
                        AND 
                            (
                                up.skill_id=?2 
                                OR up.skill_id=?3 
                                OR up.skill_id IS NULL
                            ) 
                        AND EXISTS 
                            (
                                SELECT 1 FROM user_achievement ua 
                                WHERE ua.project_id=?1 
                                    AND ua.skill_id=?3 
                                    AND ua.user_id = up.user_id
                            )''', nativeQuery = true)
    void updateAchievedSkillPoints(String projectId, String subjectId, String skillId, int pointDelta)

    @Modifying
    @Query(value = '''
            WITH skill AS (
                select case when copied_from_skill_ref is not null then copied_from_skill_ref else id end as id,
                       point_increment as pointIncrement
                from skill_definition
                where project_id = :projectId 
                    and skill_id = :skillId
            ),
            userPoints AS (
                SELECT ups.user_id, sum(skill.pointIncrement) as newPoints
                FROM user_performed_skill ups, skill
                where ups.skill_ref_id = skill.id
                group by ups.user_id
            )
            UPDATE user_points
            SET points = userPoints.newPoints
            FROM userPoints
            where userPoints.user_id = user_points.user_id and project_id = :projectId and skill_id = :skillId''', nativeQuery=true)
    void updateUserPointsForASkill(@Param("projectId") String projectId, @Param("skillId") String skillId)

    @Modifying
    @Query(value = '''
            WITH skill AS (
                select case when copied_from_skill_ref is not null then copied_from_skill_ref else id end as id,
                       point_increment as pointIncrement
                from skill_definition
                where project_id = :projectId and skill_id = :skillId
            ),
            userPoints AS (
                SELECT ups.user_id, sum(skill.pointIncrement) as newPoints
                FROM user_performed_skill ups, skill
                where ups.skill_ref_id = skill.id
                group by ups.user_id
            )
            UPDATE user_points
            SET points = (select userPoints.newPoints from userPoints where userPoints.user_id = user_points.user_id)
            where project_id = :projectId
                and skill_id = :skillId
                and exists( select * from userPoints where userPoints.user_id = user_points.user_id)''', nativeQuery=true)
    void updateUserPointsForASkillInH2(@Param("projectId") String projectId, @Param("skillId") String skillId)


    @Modifying
    @Query(value = '''
            WITH skills AS (
                select case when child.copied_from_skill_ref is not null then child.copied_from_skill_ref else child.id end as id,
                       child.point_increment as pointIncrement
                from skill_definition parent,
                     skill_relationship_definition rel,
                     skill_definition child
                where parent.project_id = :projectId
                  and parent.skill_id = :skillId
                  and rel.parent_ref_id = parent.id
                  and rel.child_ref_id = child.id
                  and rel.type in ('RuleSetDefinition', 'SkillsGroupRequirement')
                  and child.type = 'Skill'
                  and child.enabled = 'true'
            ),
            userPoints AS (
                SELECT ups.user_id, sum(skills.pointIncrement) as newPoints
                FROM user_performed_skill ups,
                     skills
                where ups.skill_ref_id = skills.id
                group by ups.user_id
            )
            UPDATE user_points
            SET points = userPoints.newPoints
            FROM userPoints
            where userPoints.user_id = user_points.user_id and project_id = :projectId and skill_id = :skillId''', nativeQuery=true)
    void updateUserPointsForASubjectOrGroup(@Param("projectId") String projectId, @Param("skillId") String skillId)

    @Modifying
    @Query(value = '''
               WITH pointsToUpdate as (
                    select up.user_id, sum(up.points) as newPoints
                    from skill_definition sd,
                    user_points up
                    where sd.id = up.skill_ref_id
                          and up.project_id = :projectId
                          and sd.type = 'Subject'
                    group by up.user_id
                )
                update user_points up set points = pointsToUpdate.newPoints
                        from pointsToUpdate
                where up.user_id = pointsToUpdate.user_id
                and up.project_id = :projectId and up.skill_id is null;''', nativeQuery=true)
    void updateUserPointsForProject(@Param("projectId") String projectId)

    @Modifying
    @Query(value = '''
               WITH pointsToUpdate as (
                    select up.user_id, sum(up.points) as newPoints
                    from skill_definition sd,
                         user_points up
                    where sd.id = up.skill_ref_id
                      and sd.project_id = :projectId
                      and sd.type = 'Subject'
                    group by up.user_id
                )
                update user_points up
                set points = (select pointsToUpdate.newPoints
                              from pointsToUpdate
                              where up.user_id = pointsToUpdate.user_id)
                where up.project_id = :projectId
                  and up.skill_id is null
                  and exists(select *
                             from pointsToUpdate
                             where up.user_id = pointsToUpdate.user_id);''', nativeQuery=true)
    void updateUserPointsForProjectInH2(@Param("projectId") String projectId)

    @Modifying
    @Query(value = '''
             WITH pointsToUpdate as (
                select up.user_id userId, sum(up.points) as newPoints
                from skill_definition parent,
                     skill_relationship_definition rel,
                     skill_definition child,
                     user_points up
                where parent.project_id = :projectId
                  and parent.skill_id = :skillId
                  and rel.parent_ref_id = parent.id
                  and rel.child_ref_id = child.id
                  and rel.type in ('RuleSetDefinition', 'GroupSkillToSubject')
                  and child.type = 'Skill'
                  and (child.enabled = 'true' or 'false' = :enabledSkillsOnly)
                  and child.id = up.skill_ref_id
                group by up.user_id
            )
            update user_points up set points = pointsToUpdate.newPoints
            from pointsToUpdate
            where up.user_id = pointsToUpdate.userId
              and up.project_id = :projectId and up.skill_id = :skillId''', nativeQuery = true)
    void updateSubjectUserPoints(@Param("projectId") String projectId, @Param("skillId") String skillId, @Param('enabledSkillsOnly') Boolean enabledSkillsOnly)

    @Modifying
    @Query(value = '''delete from user_points up 
            where 
                up.project_id = :projectId
                and up.skill_id = :subjectId
                and up.user_id not in (
                     select up.user_id userId
                        from skill_definition parent,
                             skill_relationship_definition rel,
                             skill_definition child,
                             user_points up
                        where parent.project_id = :projectId
                          and parent.skill_id = :subjectId
                          and rel.parent_ref_id = parent.id
                          and rel.child_ref_id = child.id
                          and rel.type in ('RuleSetDefinition', 'GroupSkillToSubject')
                          and child.type = 'Skill'
                          and child.id = up.skill_ref_id
                        group by up.user_id
                )
            ''', nativeQuery = true)
    void removeSubjectUserPointsForNonExistentSkillDef(@Param("projectId") String projectId, @Param("subjectId") String subjectId)

    @Modifying
    @Query(value = '''
             WITH pointsToUpdate as (
                select up.user_id userId, sum(up.points) as newPoints
                from skill_definition parent,
                     skill_relationship_definition rel,
                     skill_definition child,
                     user_points up
                where parent.project_id = :projectId
                  and parent.skill_id = :skillId
                  and rel.parent_ref_id = parent.id
                  and rel.child_ref_id = child.id
                  and rel.type in ('RuleSetDefinition', 'GroupSkillToSubject')
                  and child.type = 'Skill'
                  and (child.enabled = 'true' or 'false' = :enabledSkillsOnly)
                  and child.id = up.skill_ref_id
                group by up.user_id
            )
            update user_points up set points = (select pointsToUpdate.newPoints from pointsToUpdate where pointsToUpdate.userId = up.user_id)
            where up.project_id = :projectId and up.skill_id = :skillId
              and exists (select * from pointsToUpdate where pointsToUpdate.userId = up.user_id);''', nativeQuery=true)
    void updateSubjectUserPointsInH2(@Param("projectId") String projectId, @Param("skillId") String skillId, @Param('enabledSkillsOnly') Boolean enabledSkillsOnly)

    @Modifying
    @Query('''delete from UserPoints where projectId = :projectId and points = 0''')
    void deleteZeroPointEntries(@Param("projectId") String projectId)

    @Modifying
    @Query('''delete from UserPoints up where up.projectId = :projectId and 
                                                up.userId in (
                                                    select upp.userId 
                                                    from UserPoints upp 
                                                    where upp.projectId = :projectId 
                                                    group by upp.userId 
                                                    having count(upp.id) = 1
                                                )''')
    void removeOrphanedProjectPoints(@Param("projectId") String projectId)
}
