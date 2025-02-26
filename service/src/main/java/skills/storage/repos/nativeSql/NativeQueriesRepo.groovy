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
package skills.storage.repos.nativeSql


import skills.storage.model.QueryUsersCriteria
import skills.storage.model.SkillDef
import skills.storage.model.SkillDefPartial
import skills.storage.model.SkillsDBLock

import java.util.stream.Stream

interface NativeQueriesRepo {
    void decrementPointsForDeletedSkill(String projectId, String deletedSkillId, String parentSubjectSkillId)

    void updateOverallScoresBySummingUpAllChildSubjects(String projectId, SkillDef.ContainerType subjectType)

    List<GraphRelWithAchievement> getDependencyGraphWithAchievedIndicator(String projectId, String skillId, String userId)

    void updatePointTotalsForSkill(String projectId, String subjectId, String skillId, int incrementDelta)

    void updatePointTotalWhenOccurrencesAreDecreased(String projectId, String subjectId, String skillId, int pointIncrement, int newOccurrences, int previousOccurrences)

    void removeExtraEntriesOfUserPerformedSkillByUser(String projectId, String skillId, int numEventsToKeep)

    void removeUserAchievementsThatDoNotMeetNewNumberOfOccurrences(String projectId, String skillId, int numOfOccurrences)

    int addBadgeAchievementForEligibleUsers(String projectId, String badgeId, Integer badgeRowId, Boolean notified, Date start, Date end)

    int addGlobalBadgeAchievementForEligibleUsers(String globalBadgeId,
                                                  Integer globalBadgeRowId,
                                                  Boolean notified,
                                                  Integer requiredSklls,
                                                  Integer requiredLevels,
                                                  Date start,
                                                  Date end)

    void createOrUpdateUserEvent(String projectId, Integer skillRefId, String userId, Date start, String type, Integer count, Integer weekNumber)

    long countUsers(QueryUsersCriteria queryUsersCriteria)

    Stream<String> getUserIds(QueryUsersCriteria queryUsersCriteria)

    void updateUserPointsForASkill(String projectId, String skillId)

    void updateUserPointsForSubject(String projectId, String skillId, Boolean enabledSkillsOnly)

    void updateUserPointsForProject(String projectId)

    SkillsDBLock insertLockOrSelectExisting(String lockKey)

    Long countDistinctUsersByProjectIdAndSubjectIdAndUserIdLike(String projectId, String subjectId, String userId)

    Long countDistinctUsersByProjectIdAndSubjectId(String projectId, String subjectId)

    List<SkillDefPartial> getSkillsWithCatalogStatusExplodeSkillGroups(String projectId, String subjectId)
}

