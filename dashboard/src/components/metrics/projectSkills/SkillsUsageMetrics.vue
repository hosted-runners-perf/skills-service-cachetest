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
  <metrics-card title="Skills" :no-padding="true" data-cy="skillsNavigator">
    <div class="row px-3 pt-3">
      <div class="col-12 col-md border-right">
        <b-form-group label="Skill Name Filter" label-class="text-muted">
          <b-input v-model="filters.name" v-on:keydown.enter="applyFilters" data-cy="skillsNavigator-skillNameFilter" aria-label="skill name filter"/>
        </b-form-group>
      </div>
      <div class="col-md" data-cy="skillsNavigator-filters">
        <b-form-group label="Tag Filters"  label-class="text-muted">
          <b-form-checkbox v-model="filters.overlookedTag" inline>
            <b-badge variant="danger" class="ml-2">Overlooked Skill</b-badge>
          </b-form-checkbox>
          <b-form-checkbox v-model="filters.topSkillTag" inline>
            <b-badge variant="info" class="ml-2">Top Skill</b-badge>
          </b-form-checkbox>
          <b-form-checkbox v-model="filters.highActivityTag" inline>
            <b-badge variant="success" class="ml-2">High Activity</b-badge>
          </b-form-checkbox>
          <b-form-checkbox v-model="filters.neverAchieved" inline>
            <b-badge variant="warning" class="ml-2">Never Achieved</b-badge>
          </b-form-checkbox>
          <b-form-checkbox v-model="filters.neverReported" inline>
            <b-badge variant="warning" class="ml-2">Never Reported</b-badge>
          </b-form-checkbox>
          <div class="text-muted small">Please Note: Tags become more meaningful with extensive usage</div>
        </b-form-group>
      </div>
    </div>
    <div class="row pl-3 mb-3">
      <div class="col">
        <b-button variant="outline-info" @click="applyFilters" data-cy="skillsNavigator-filterBtn"><i class="fa fa-filter"/> Filter</b-button>
        <b-button variant="outline-info" @click="reset" class="ml-1" data-cy="skillsNavigator-resetBtn"><i class="fa fa-times"/> Reset</b-button>
      </div>
    </div>

    <skills-b-table :items="items" :options="tableOptions" data-cy="skillsNavigator-table" tableStoredStateId="skillsNavigator-table">
      <template v-slot:cell(skillName)="data">
        <span class="ml-2">{{ data.value }} <b-badge v-if="data.item.isReusedSkill"
                                                     variant="success" class="text-uppercase"><i
          class="fas fa-recycle"></i> Reused</b-badge></span>
        <b-button-group class="float-right">
          <b-button target="_blank" :to="{ name: 'SkillOverview', params: { projectId: projectId, subjectId: data.item.subjectId, skillId: data.item.skillId } }"
                    variant="outline-info" size="sm" class="text-secondary"
                    v-b-tooltip.hover="'View Skill Configuration'"><i class="fa fa-wrench"/><span class="sr-only">view skill configuration</span></b-button>
          <b-button :id="`b-skill-metrics_${data.item.skillId}`" target="_blank" :to="{ name: 'SkillMetrics', params: { projectId: projectId, subjectId: data.item.subjectId, skillId: data.item.skillId } }"
                    variant="outline-info" size="sm" class="text-secondary"
                    v-b-tooltip.hover="'View Skill Metrics'"><i class="fa fa-chart-bar"/><span class="sr-only">view skill metrics</span></b-button>
        </b-button-group>
      </template>

      <template v-slot:cell(numUserAchieved)="data">
        <span class="ml-2">{{ data.value | number}}</span>
        <b-badge v-if="data.item.isOverlookedTag" variant="danger" class="ml-2">Overlooked Skill</b-badge>
        <b-badge v-if="data.item.isTopSkillTag" variant="info" class="ml-2">Top Skill</b-badge>
      </template>

      <template v-slot:cell(numUsersInProgress)="data">
        <span class="ml-2">{{ data.value | number }}</span>
        <b-badge v-if="data.item.isHighActivityTag" variant="success" class="ml-2">High Activity</b-badge>
      </template>

      <template v-slot:cell(lastAchievedTimestamp)="data">
        <b-badge v-if="data.item.isNeverAchievedTag" variant="warning" class="ml-2">Never</b-badge>
        <div v-else>
          <div>
            {{ data.value | date }}
          </div>
          <div class="text-muted small">
            <span>{{ data.value | timeFromNow }}</span>
          </div>
        </div>
      </template>

      <template v-slot:cell(lastReportedTimestamp)="data">
        <b-badge v-if="data.item.isNeverReportedTag" variant="warning" class="ml-2">Never</b-badge>
        <div v-else>
          <div>
            <span>{{ data.value | date }}</span>
          </div>
          <div class="text-muted small">
            {{ data.value | timeFromNow }}
          </div>
        </div>
      </template>
    </skills-b-table>
  </metrics-card>
</template>

<script>
  import SkillsBTable from '@/components/utils/table/SkillsBTable';
  import MetricsService from '../MetricsService';
  import MetricsCard from '../utils/MetricsCard';
  import SkillsUsageHelper from './SkillsUsageHelper';

  export default {
    name: 'SkillsUsageMetrics',
    components: { MetricsCard, SkillsBTable },
    data() {
      return {
        projectId: this.$route.params.projectId,
        filters: {
          name: '',
          highActivityTag: false,
          overlookedTag: false,
          topSkillTag: false,
          neverAchieved: false,
          neverReported: false,
        },
        tableOptions: {
          busy: false,
          sortBy: 'timestamp',
          sortDesc: true,
          bordered: true,
          outlined: true,
          rowDetailsControls: false,
          stacked: 'md',
          fields: [
            {
              key: 'skillName',
              label: 'Skill',
              sortable: true,
            },
            {
              key: 'numUserAchieved',
              sortable: true,
              label: '# Users Achieved',
            },
            {
              key: 'numUsersInProgress',
              sortable: true,
              label: '# Users In Progress',
            },
            {
              key: 'lastReportedTimestamp',
              sortable: true,
              label: 'Last Reported',
            },
            {
              key: 'lastAchievedTimestamp',
              sortable: true,
              label: 'Last Achieved',
            },
          ],
          pagination: {
            currentPage: 1,
            totalRows: 1,
            pageSize: 5,
            possiblePageSizes: [5, 10, 15, 20, 50],
          },
          tableDescription: 'Skill Metrics',
        },
        items: [],
        originalItems: [],
      };
    },
    mounted() {
      this.loadData();
    },
    methods: {
      applyFilters() {
        this.items = this.originalItems.filter((item) => SkillsUsageHelper.shouldKeep(this.filters, item));
      },
      reset() {
        this.filters.name = '';
        this.filters.neverAchieved = false;
        this.filters.neverReported = false;
        this.filters.overlookedTag = false;
        this.filters.topSkillTag = false;
        this.filters.highActivityTag = false;
        this.items = this.originalItems;
      },
      loadData() {
        this.tableOptions.busy = true;
        MetricsService.loadChart(this.$route.params.projectId, 'skillUsageNavigatorChartBuilder')
          .then((dataFromServer) => {
            this.items = SkillsUsageHelper.addTags(dataFromServer);
            this.originalItems = this.items;
            this.tableOptions.pagination.totalRows = this.items.length;
            this.tableOptions.busy = false;
          });
      },
    },
  };
</script>

<style scoped>

</style>
