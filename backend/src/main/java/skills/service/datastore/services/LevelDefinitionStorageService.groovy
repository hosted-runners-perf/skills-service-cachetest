package skills.service.datastore.services

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.Validate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import skills.service.controller.exceptions.SkillException
import skills.service.controller.request.model.EditLevelRequest
import skills.service.controller.request.model.NextLevelRequest
import skills.service.controller.result.model.LevelDefinitionRes
import skills.service.controller.result.model.SettingsResult
import skills.service.datastore.services.settings.Settings
import skills.service.datastore.services.settings.SettingsService
import skills.storage.repos.LevelDefRepo
import skills.storage.repos.ProjDefRepo
import skills.storage.repos.SkillDefRepo
import skills.storage.model.LevelDef
import skills.storage.model.ProjDef
import skills.storage.model.SkillDef
import skills.storage.repos.UserAchievedLevelRepo

@Service
@Slf4j
class LevelDefinitionStorageService {

    @Value('#{"${skills.levels.max:25}"}')
    private int maxLevels

    @Autowired
    LevelDefRepo levelDefinitionRepository

    @Autowired
    ProjDefRepo projDefRepo

    @Autowired
    SkillDefRepo skillDefRepo

    @Autowired
    SettingsService settingsService

    @Autowired
    UserAchievedLevelRepo achievedLevelRepository

    // this could also come from DB.. eventually
    Map<String,Integer> defaultPercentages = ["White Belt":10, "Blue Belt":25, "Purple Belt":45, "Brown Belt":67, "Black Belt":92]


    LevelInfo getLevelInfo(SkillDef skillDefinition, int currentScore) {
        return doGetLevelInfo(skillDefinition.projectId, skillDefinition.levelDefinitions, skillDefinition.totalPoints, currentScore)
    }

    LevelInfo getOverallLevelInfo(ProjDef projDef, int currentScore) {
        LevelInfo levelInfo = doGetLevelInfo(projDef.projectId, projDef.levelDefinitions, projDef.totalPoints, currentScore)
        return levelInfo
    }

    private LevelInfo doGetLevelInfo(String projectId, List<LevelDef> levelDefinitions, int totalPoints, int currentScore) {
        SettingsResult setting = settingsService.getSetting(projectId, Settings.LEVEL_AS_POINTS.settingName)

        List<Integer> levelScores
        if(setting?.isEnabled()){
            levelScores = loadPointsLevels(levelDefinitions)
        } else {
            levelScores = loadPercentLevels(levelDefinitions, totalPoints)
        }
        LevelInfo levelInfo = calculateLevel(levelScores, levelDefinitions, currentScore)
        return levelInfo
    }

    int getPointsRequiredForLevel(SkillDef skillDefinition, int level) {
        SettingsResult setting = settingsService.getSetting(skillDefinition.projectId, Settings.LEVEL_AS_POINTS.settingName)

        List<Integer> levelScores = []
        if(setting?.isEnabled()){
            levelScores = loadPointsLevels(skillDefinition.levelDefinitions)
        } else {
            levelScores = loadPercentLevels(skillDefinition.levelDefinitions, skillDefinition.totalPoints)
        }
        return calculatePointsRequiredForLevel(levelScores, level)
    }

    int getPointsRequiredForOverallLevel(ProjDef projDef, int level) {
        SettingsResult setting = settingsService.getSetting(projDef.projectId, Settings.LEVEL_AS_POINTS.settingName)

        List<Integer> levelScores = []
        if(setting?.isEnabled()){
            levelScores = loadPointsLevels(projDef.levelDefinitions)
        } else {
            levelScores = loadPercentLevels(projDef.levelDefinitions, projDef.totalPoints)
        }
        return calculatePointsRequiredForLevel(levelScores, level)
    }

    private List<Integer> loadPercentLevels(List<LevelDef> levelDefinitions, int currentScore) {
        List<Integer> levelScores = levelDefinitions.sort({ it.level }).collect {
            return (int) (currentScore * (it.percent / 100d))
        }
        return levelScores
    }

    private List<Integer> loadPointsLevels(List<LevelDef> levelDefinitions){
        List<Integer> levelScores = levelDefinitions.sort({ it.level }).collect {
            return it.pointsFrom
        }
        return levelScores
    }

    private int calculatePointsRequiredForLevel(List<Integer> levelScores, int level) {
        if (level == 0) {
            return 0
        }

        //this can occur when a user has achieved a level
        //but levels have been deleted since their achievement
        if(level > levelScores.size()){
            level = levelScores.size()
        }
        assert level <= levelScores.size()
        return levelScores[level - 1]
    }

    private LevelInfo calculateLevel(List<Integer> levelScores, List<LevelDef> levelDefinitions, int currentScore) {
        Integer found

        levelScores.each {
            if (it <= currentScore) {
                found = it
            }
        }

        int index = -1
        if (found != null) {
            index = levelScores.indexOf(found)
        }

        int substract = 0
        if (index >= 0) {
            substract = levelScores.get(index)
        }

        // next level points are the different between my level points and previous level points
        int nextLevelPoints = -1
        if (index + 1 < levelScores.size()) {
            nextLevelPoints = levelScores.get(index + 1)
            if (index >= 0) {
                nextLevelPoints -= levelScores.get(index)
            }
        }

        LevelInfo levelInfo = new LevelInfo(
                level: index + 1,
                currentPoints: currentScore - substract,
                nextLevelPoints: nextLevelPoints,
                totalNumLevels: levelDefinitions.size()
        )
        return levelInfo
    }

    @EqualsAndHashCode
    @ToString
    static class LevelInfo implements Serializable {
        int level
        int totalNumLevels
        int currentPoints
        int nextLevelPoints
    }


    @Transactional
    List<LevelDefinitionRes> getLevels(String projectId, String skillId = null) {
        LevelDefRes levelDefRes = getLevelDefs(projectId, skillId)
        return doGetLevelsDefRes(levelDefRes.levels, levelDefRes.totalPoints, levelDefRes.projectId, levelDefRes.skillId)
    }

    @Transactional
    List<LevelDefinitionRes> getLevelsDefRes(ProjDef projDef) {
        return doGetLevelsDefRes(projDef.levelDefinitions, projDef.totalPoints, projDef.projectId)
    }

    @Transactional
    List<LevelDefinitionRes> getLevelsDefRes(SkillDef skillDef) {
        return doGetLevelsDefRes(skillDef.levelDefinitions, skillDef.totalPoints, skillDef.projectId, skillDef.skillId)
    }

    private List<LevelDefinitionRes> doGetLevelsDefRes(List<LevelDef> levelDefinitions, int totalPoints, String projectId, String skillId = null) {

        SettingsResult setting = settingsService.getSetting(projectId, Settings.LEVEL_AS_POINTS.settingName)

        List<Integer> levelScores = []
        if(!setting?.isEnabled()) {
            levelScores = levelDefinitions.sort({ it.level }).collect {
                return totalPoints > 0 ? (int) (totalPoints * (it.percent / 100d)) : null
            }
        }

        List<LevelDefinitionRes> finalRes = []
        levelDefinitions.eachWithIndex { LevelDef entry, int i ->
            Integer fromPts =  entry.pointsFrom
            Integer toPts = entry.pointsTo
            if(!setting?.isEnabled()) {
                fromPts = levelScores.get(i)
                toPts = (i != levelScores.size() - 1) ? levelScores.get(i + 1) : null
            }
            finalRes << new LevelDefinitionRes(projectId: projectId,
                    id: entry.id,
                    skillId: skillId,
                    level: entry.level,
                    percent: entry.percent,
                    pointsFrom: fromPts,
                    pointsTo: toPts,
                    name: entry.name,
                    iconClass: entry.iconClass,
                    achievable: fromPts <= totalPoints
                    )
        }
        return finalRes
    }

    private static class LevelDefRes {
        List<LevelDef> levels
        String projectId
        String skillId
        int totalPoints
    }

    private LevelDefRes getLevelDefs(String projectId, String skillId = null) {
        LevelDefRes res

        if (skillId) {
            SkillDef skillDef = skillDefRepo.findByProjectIdAndSkillIdAndType(projectId, skillId, SkillDef.ContainerType.Subject)
            if(!skillDef){
                throw new SkillException("Failed to find Ability for project", projectId, skillId)
            }
            res = new LevelDefRes(levels: skillDef.levelDefinitions?.sort({it.level}), projectId: skillDef.projectId, skillId: skillDef.skillId, totalPoints: skillDef.totalPoints)
        } else {
            ProjDef projDef = projDefRepo.findByProjectId(projectId)
            if(!projDef){
                throw new SkillException("Failed to find project", projectId, null)
            }
            res = new LevelDefRes(levels: projDef.levelDefinitions?.sort({it.level}), projectId: projDef.projectId, totalPoints: projDef.totalPoints)
        }
        return res
    }

    /**
     * deletes the highest level and returns its definition
     * @param projectId
     * @return
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    LevelDef deleteLastLevel(String projectId, String skillId = null) {
        LevelDef removed
        LevelDefRes result = getLevelDefs(projectId, skillId)
        List<LevelDef> existingDefinitions = result?.levels

        if (existingDefinitions.size() == 1) {
            throw new SkillException("A minimum of one level is required", projectId, skillId)
        }

        if (existingDefinitions) {
            existingDefinitions = existingDefinitions.sort({ it.level })
            removed = existingDefinitions.last()

            int usersAtLevel = achievedLevelRepository.countByProjectIdAndSkillIdAndLevel(projectId, skillId, removed.level)
            if(usersAtLevel > 0){
                throw new SkillException("Unable to delete level ${removed.level}, $usersAtLevel ${usersAtLevel > 1 ? 'users have' : 'user has'} achieved this level")
            }

            levelDefinitionRepository.deleteById(removed.id)
            log.info("Deleted last level [{}]", removed)
            //now we have to null out the toPoints of the 2nd to last level
            if(existingDefinitions.size() > 1){
                LevelDef alter = existingDefinitions.get(existingDefinitions.size()-2)
                alter.pointsTo = null
                levelDefinitionRepository.save(alter)
            }
        }

        return removed
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    LevelDef editLevel(String projectId, EditLevelRequest editLevelRequest, String levelId, String skillId = null) {
        SettingsResult setting = settingsService.getSetting(projectId, Settings.LEVEL_AS_POINTS.settingName)
        assert editLevelRequest.name?.length() <= 50
        boolean asPoints = false
        if(setting?.isEnabled()){
            asPoints = true
        }
        LevelValidator.validateEditRequest(editLevelRequest, asPoints)

        LevelDefRes existingDefinitions = getLevelDefs(projectId, skillId)
        existingDefinitions.levels.sort({it.level})

        int toEditIndex = existingDefinitions.levels.findIndexOf { it.id == editLevelRequest.id }
        LevelDef toEdit = null

        //validate that the edit doesn't break the consistency of the other levels.
        for(int i=0; i < existingDefinitions.levels.size(); i++){
            def levelDef = existingDefinitions.levels.get(i)
            if(i < toEditIndex){
                LevelValidator.validateLevelsBefore(levelDef, editLevelRequest, asPoints)
            }else if(i == toEditIndex){
                toEdit = levelDef
            }else if(i > toEditIndex){
                LevelValidator.validateLevelsAfter(levelDef, editLevelRequest, asPoints)
            }
        }

        assert toEdit != null
        if(asPoints){
            toEdit.pointsFrom = editLevelRequest.pointsFrom
            toEdit.pointsTo = editLevelRequest.pointsTo
        }else{
            toEdit.percent = editLevelRequest.percent
        }

        toEdit.level = editLevelRequest.level
        toEdit.name = editLevelRequest.name
        toEdit.iconClass = editLevelRequest.iconClass
        toEdit = levelDefinitionRepository.save(toEdit)

        return toEdit
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    LevelDef addNextLevel(String projectId, NextLevelRequest nextLevelRequest, String skillId = null) {
        SettingsResult setting = settingsService.getSetting(projectId, Settings.LEVEL_AS_POINTS.settingName)
        assert nextLevelRequest.name?.length() <= 50
        boolean asPoints = false
        if(setting?.isEnabled()){
            asPoints = true
            assert nextLevelRequest?.points > 0
        }

        LevelDef created
        LevelDefRes existingDefinitions = getLevelDefs(projectId, skillId)

        if(existingDefinitions.levels.size() == maxLevels){
            throw new SkillException("No more then $maxLevels levels are allowed", projectId, skillId)
        }

        if(asPoints) {
            assert existingDefinitions.levels.collect({ it.pointsTo }).max() < nextLevelRequest.points
        }else{
            assert existingDefinitions.levels.collect({ it.percent }).max() < nextLevelRequest.percent
        }

        if (existingDefinitions) {
            LevelDef lastOne = existingDefinitions.levels.sort({ it.level }).last()
            SkillDef skill = null
            ProjDef project = null
            if(skillId) {
                skill = skillDefRepo.findByProjectIdAndSkillIdAndType(projectId, skillId, SkillDef.ContainerType.Subject)
                assert skill
            }else{
                project = projDefRepo.findByProjectId(projectId)
                assert project
            }

            if(asPoints){
                //if the system is configured to use points instead of percentages for levels
                //then we need to explicitly update the toRange of the previous highest level
                lastOne.pointsTo = nextLevelRequest.points
                levelDefinitionRepository.save(lastOne)
            }

            created = new LevelDef(level: lastOne.level + 1,
                    projDef: project,
                    percent: nextLevelRequest.percent,
                    skillDef: skill,
                    name: nextLevelRequest.name,
                    pointsFrom: nextLevelRequest.points,
                    iconClass: nextLevelRequest.iconClass
            )

            created = levelDefinitionRepository.save(created)

            if(skill){
                skill.addLevel(created)
                skillDefRepo.save(skill)
            }else{
                project.addLevel(created)
                projDefRepo.save(project)
            }
            log.info("Added new level [{}]", created)
        }

        return created
    }

    List<LevelDef> createDefault(String projectId) {
        SettingsResult setting = settingsService.getSetting(projectId, Settings.LEVEL_AS_POINTS.settingName)

        List<LevelDef> res = []
        int i=0
        defaultPercentages.each { String name, Integer percentage ->
            def levelDef = new LevelDef(level: ++i, percent: percentage, name: name, iconClass: "fas fa-user-ninja")
            log.info("creating default level $levelDef")
            res << levelDef
        }
        if(setting?.isEnabled()){
            new LevelUtils().convertToPoints(res, LevelUtils.defaultTotalPointsGuess)
        }

        levelDefinitionRepository.saveAll(res)
    }
}
