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
    <div>
        <skills-spinner :loading="loading" />

        <div v-if="!loading">
            <skills-title>Global Badge Details</skills-title>

            <div class="card">
                <div class="card-body">
                    <badge-details-overview :badge="badgeSummary"></badge-details-overview>
                </div>
              <div v-if="badge.helpUrl" class="card-footer text-left">
                <a :href="badge.helpUrl" target="_blank" rel="noopener" class="btn btn-sm btn-outline-info skills-theme-btn">
                  Learn More <i class="fas fa-external-link-alt"></i>
                </a>
              </div>
            </div>

            <div v-for="projectSummary in projectSummaries" :key="projectSummary.projectId" class="card mt-1" :data-cy="'gb_'+projectSummary.projectId">
                <h4 class="card-header text-sm-left text-secondary text-center col">{{ projectDisplayName }}: {{ projectSummary.projectName }}</h4>
                <div class="card-body">
                    <project-level-row v-if="projectSummary && projectSummary.projectLevel" :projectLevel="projectSummary.projectLevel" />
                    <skills-progress-list v-if="projectSummary && projectSummary.skills"
                                          @points-earned="refreshHeader"
                                          :subject="badge" :projectId="projectSummary.projectId"
                                          :show-descriptions="showDescriptions"
                                          :show-no-data-msg="false"
                                          type="global-badge"/>
                </div>
            </div>
            <div v-if="!(projectSummaries && projectSummaries.length > 0)">
                <no-data-yet class="my-2"
                             :title="`No ${this.skillDisplayName}s or ${this.projectDisplayName} ${this.levelDisplayName}s have not been added yet.`" :sub-title="`Please contact a ${this.projectDisplayName} Administrator.`"/>
            </div>
        </div>
    </div>
</template>

<script>
  import BadgeDetailsOverview from '@/common-components/badges/BadgeDetailsOverview';
  import NoDataYet from '@/common-components/utilities/NoDataYet';
  import SkillsProgressList from '@/userSkills/skill/progress/SkillsProgressList';
  import ProjectLevelRow from '@/userSkills/badge/ProjectLevelRow';
  import SkillsSpinner from '@/common/utilities/SkillsSpinner';
  import UserSkillsService from '@/userSkills/service/UserSkillsService';
  import SkillsTitle from '@/common/utilities/SkillsTitle';

  export default {
    components: {
      SkillsTitle,
      SkillsProgressList,
      BadgeDetailsOverview,
      SkillsSpinner,
      ProjectLevelRow,
      NoDataYet,
    },
    data() {
      return {
        loading: true,
        badge: null,
        badgeSummary: null,
        initialized: false,
        showDescriptions: false,
      };
    },
    computed: {
      projectSummaries() {
        return this.badge.projectLevelsAndSkillsSummaries.map((item) => ({
          badge: this.badge,
          badgeId: this.badge.badgeId,
          projectId: item.projectId,
          projectName: item.projectName,
          skills: item.skills,
          projectLevel: item.projectLevel,
        }));
      },
    },
    watch: {
      $route: 'fetchData',
    },
    mounted() {
      this.fetchData();
    },
    methods: {
      fetchData() {
        UserSkillsService.getBadgeSkills(this.$route.params.badgeId, true)
          .then((badgeSummary) => {
            this.badge = badgeSummary;
            this.badgeSummary = badgeSummary;
            this.loading = false;
          });
      },
      refreshHeader(event) {
        if (event.badgeId && event.badgeId === this.badge.badgeId) {
          UserSkillsService.getBadgeSkills(this.$route.params.badgeId, true, false)
            .then((badgeSummary) => {
              this.badgeSummary = badgeSummary;
            });
        }
      },
    },
  };
</script>

<style scoped>

</style>
