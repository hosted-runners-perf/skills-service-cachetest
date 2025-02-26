/*
Copyright 2020 SkillTree

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
<template>
    <div class="text-primary">
        <div v-if="!loading.dependencies && !loading.skill">
            <skills-title>{{ skillDisplayName }} Overview</skills-title>
            <div class="card">
              <div class="pageControl" v-if="skill && (skill.prevSkillId || skill.nextSkillId) && !isCrossProject">
                <button @click="prevButtonClicked" v-if="skill.prevSkillId" type="button" class="btn btn-outline-info skills-theme-btn m-0 prevButton" data-cy="prevSkill"
                  aria-label="previous skill">
                  <i class="fas fa-arrow-alt-circle-left"></i>
                  Previous
                  <span class="sr-only">Previous</span>
                </button>
                <span style="font-size: 0.9rem;" data-cy="skillOrder"><span class="font-italic">{{ skillDisplayName }}</span> <b>{{ skill.orderInGroup }}</b> <span class="font-italic">of</span> <b>{{ skill.totalSkills }}</b></span>
                <button @click="nextButtonClicked" v-if="skill.nextSkillId" type="button" class="btn btn-outline-info skills-theme-btn m-0 nextButton" data-cy="nextSkill"
                  aria-label="next skill">
                  Next
                  <i class="fas fa-arrow-alt-circle-right"></i>
                  <span class="sr-only">Next</span>
                </button>
              </div>
              <div class="card-body text-center text-sm-left">
                <skill-progress2 :skill="skill" @points-earned="onPointsEarned" />
              </div>
            </div>
            <skill-dependencies class="mt-2" v-if="dependencies && dependencies.length > 0" :dependencies="dependencies"
                                :skill-id="$route.params.skillId" :subject-id="this.$route.params.subjectId"></skill-dependencies>
        </div>
        <div v-else>
            <skills-spinner :loading="loading.dependencies || loading.skill" class="mt-5"/>
        </div>
    </div>
</template>

<script>
  import store from '@/store/store';
  import UserSkillsService from '@/userSkills/service/UserSkillsService';
  import SkillsSpinner from '@/common/utilities/SkillsSpinner';
  import SkillsTitle from '@/common/utilities/SkillsTitle';
  import SkillProgress2 from '@/userSkills/skill/progress/SkillProgress2';
  import NavigationErrorMixin from '@/common/utilities/NavigationErrorMixin';
  import SkillEnricherUtil from '../utils/SkillEnricherUtil';
  import SkillHistoryUtil from '../utils/SkillHistoryUtil';

  export default {
    name: 'SkillDetails',
    mixins: [NavigationErrorMixin],
    components: {
      SkillsTitle,
      'skill-dependencies': () => import(/* webpackChunkName: 'skillDependencies' */'@/userSkills/skill/dependencies/SkillDependencies'),
      SkillsSpinner,
      SkillProgress2,
    },
    data() {
      return {
        loading: {
          dependencies: true,
          skill: true,
        },
        dependencies: [],
        skill: {},
      };
    },
    mounted() {
      this.loadData();
    },
    watch: {
      $route: 'loadData',
    },
    computed: {
      skillDisplayName() {
        return store.getters.skillDisplayName;
      },
      isCrossProject() {
        const routeName = this.$route.name;
        return routeName === 'crossProjectSkillDetails' || this.$route.params.crossProjectId;
      },
    },
    methods: {
      genLink(b) {
        return { name: b.global ? 'globalBadgeDetails' : 'badgeDetails', params: { badgeId: b.badgeId } };
      },
      loadData() {
        this.loading.dependencies = true;
        this.loading.skill = true;
        this.dependencies = [];
        this.skill = {};
        this.loadDependencies();
        this.loadSkillSummary();
      },
      loadDependencies() {
        if (!this.$route.params.crossProjectId) {
          const skillId = this.isDependency() ? this.$route.params.dependentSkillId : this.$route.params.skillId;
          UserSkillsService.getSkillDependencies(skillId)
            .then((res) => {
              this.dependencies = res.dependencies;
              this.loading.dependencies = false;
            });
        } else {
          this.loading.dependencies = false;
        }
      },
      loadSkillSummary() {
        const skillId = this.isDependency() ? this.$route.params.dependentSkillId : this.$route.params.skillId;
        UserSkillsService.getSkillSummary(skillId, this.$route.params.crossProjectId, this.$route.params.subjectId)
          .then((res) => {
            this.skill = res;
            this.loading.skill = false;
            if (skillId && this.skill.projectId && !this.isCrossProject) {
              SkillHistoryUtil.updateSkillHistory(this.skill.projectId, skillId);
            }
          });
      },
      onPointsEarned(pts) {
        this.skill = SkillEnricherUtil.addPts(this.skill, pts);
      },
      isDependency() {
        const routeName = this.$route.name;
        return routeName === 'crossProjectSkillDetails';
      },
      prevButtonClicked() {
        const params = { skillId: this.skill.prevSkillId, projectId: this.$route.params.projectId };
        this.handlePush({
          name: 'skillDetails',
          params,
        });
      },
      nextButtonClicked() {
        const params = { skillId: this.skill.nextSkillId, projectId: this.$route.params.projectId };
        this.handlePush({
          name: 'skillDetails',
          params,
        });
      },
    },
  };
</script>

<style scoped>
.pageControl {
  width: 100%;
  padding: 8px 20px 0px;
}

.prevButton {
  float: left;
}

.nextButton {
  float: right;
}

/deep/ div.skills-badge-icon {
  text-align: center;
}
</style>
