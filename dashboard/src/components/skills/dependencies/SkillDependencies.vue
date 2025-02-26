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
    <sub-page-header title="Dependencies"/>

    <b-card body-class="p-0" class="dependencies-container">
      <loading-container :is-loading="isLoading">

        <no-content2 v-if="skill.sharedToCatalog"
                     class="mt-5 pt-5"
                     icon="fas fa-book"
                     title="Exported to the Catalog"
                     message="Once a Skill has been exported to the catalog, Dependencies may not be added."></no-content2>

        <no-content2 v-if="skill.thisSkillWasReusedElsewhere"
                     class="mt-5 pt-5"
                     icon="fas fa-recycle"
                     title="Skill was Reused"
                     message="Once a Skill has been reused, Dependencies may not be added."></no-content2>

        <div class="p-3" v-if="!skill.sharedToCatalog && !skill.thisSkillWasReusedElsewhere">
          <skills-selector2 v-if="!isReadOnlyProj" :options="allSkills" :selected="skills" v-on:added="skillAdded"
                            v-on:removed="skillDeleted"
                            data-cy="depsSelector">
            <template #dropdown-item="{ option }">
              <div class="media" data-cy="skillsSelector">
                <div class="d-inline-block mt-1 mr-3">
                  <i v-if="option.otherProjectId" class="fas fa-w-16 fa-handshake text-hc"></i>
                  <i v-else class="fas fa-w-16 fa-list-alt text-info"></i>
                </div>
                <div class="media-body">
                  <strong class="mb-2"><span v-if="option.otherProjectId"
                                             class="">{{ option.otherProjectName }} : </span>
                    {{ option.name }}</strong>
                  <div style="font-size: 0.95rem;" class="row text-secondary skills-option-id">
                    <div class="col-md">
                      <span class="font-italic">ID:</span> <span class="ml-1" data-cy="skillsSelector-skillId">{{option.skillId}}</span>
                    </div>
                    <div class="col-md">
                      <span v-if="option.otherProjectId" class="text-warning ml-3">** Shared Skill **</span>
                      <span v-else class="ml-2">
                        <span class="font-italic">Version:</span>
                        <span class="ml-1">{{option.version}}</span>
                        <span v-if="option.version > skill.version" class="text-danger ml-3"><br class="d-lg-none"/>** Not Eligible due to later version**</span>
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </template>

            <template slot="selected-item" slot-scope="{ props }">
              <span class="mt-2 mr-2 border-hc rounded px-1" style="padding-top: 2px; padding-bottom: 2px;"
                    v-bind:style="{'background-color': props.option.isFromAnotherProject ? '#ffb87f' : 'lightblue'}">
                <span class="skills-handle-overflow" style="width: 15rem;"
                      :title="props.option.isFromAnotherProject ? props.option.projectId + ' : ' + props.option.name : props.option.name">
                  <span v-if="props.option.isFromAnotherProject">{{ props.option.projectId | truncate(10)}} : </span>
                  {{ props.option.name }}
                </span>
                <button class="btn btn-sm btn-outline-secondary p-0 border-0 ml-1"
                        v-on:click="props.remove(props.option)"><i class="fas fa-times"/></button>
              </span>
            </template>
          </skills-selector2>

          <b-alert v-if="errNotification.enable" variant="danger" class="mt-2" show dismissible>
            <i class="fa fa-exclamation-circle mr-1"></i> <strong>Error!</strong> Request could not be completed! <strong>{{
            errNotification.msg }}</strong>
          </b-alert>

          <dependants-graph :skill="skill" :dependent-skills="skills" :graph="graph" class="my-3"/>
        </div>

        <simple-skills-table v-if="projConfig" :skills="skills" v-on:skill-removed="deleteSkill" :is-read-only="isReadOnlyProj">
            <span slot="name-cell" slot-scope="row">
              <i v-if="row.props.isFromAnotherProject" class="fas fa-w-16 fa-handshake text-primary mr-1"></i>
              <i v-else class="fas fa-w-16 fa-list-alt text-hc mr-1"></i>
              {{ row.props.name }}
               <div v-if="row.props.isFromAnotherProject" class="">
                Cross Project Dependency from project [{{row.props.projectId}}]
              </div>
            </span>
        </simple-skills-table>

      </loading-container>
    </b-card>
  </div>
</template>

<script>
  import { SkillsReporter } from '@skilltree/skills-client-vue';
  import NoContent2 from '@/components/utils/NoContent2';
  import SkillsService from '@/components/skills/SkillsService';
  import DependantsGraph from '@/components/skills/dependencies/DependantsGraph';
  import SkillsSelector2 from '@/components/skills/SkillsSelector2';
  import SimpleSkillsTable from '@/components/skills/SimpleSkillsTable';
  import SubPageHeader from '@/components/utils/pages/SubPageHeader';
  import LoadingContainer from '@/components/utils/LoadingContainer';
  import MsgBoxMixin from '@/components/utils/modal/MsgBoxMixin';
  import ProjConfigMixin from '@/components/projects/ProjConfigMixin';

  export default {
    name: 'SkillDependencies',
    mixins: [MsgBoxMixin, ProjConfigMixin],
    components: {
      NoContent2,
      LoadingContainer,
      SubPageHeader,
      SimpleSkillsTable,
      SkillsSelector2,
      DependantsGraph,
    },
    data() {
      return {
        skill: {},
        loading: {
          finishedDependents: false,
          finishedAllSkills: false,
        },
        errNotification: {
          enable: false,
          msg: '',
        },
        skills: [],
        previousSkills: [],
        graph: {},
        allSkills: [],
      };
    },
    watch: {
      // Vue caches components and when re-directed to the same component the path will be pushed
      // to the url but the component will NOT be re-mounted therefore we must listen for events and re-load
      // the data; alternatively could update
      //    <router-view :key="$route.fullPath"/>
      // but components will never get cached - caching maybe important for components that want to update
      // the url so the state can be re-build later (example include browsing a map or dependency graph in our case)
      '$route.params.skillId': function skillChange() {
        this.initData();
      },
    },
    mounted() {
      this.loadData();
    },
    computed: {
      isLoading() {
        return !this.loading.finishedAllSkills || !this.loading.finishedDependents || this.isLoadingProjConfig;
      },
    },
    methods: {
      initData() {
        this.errNotification.enable = false;
        this.loadAllSkills();
        this.loadDependentSkills();
      },
      loadData() {
        SkillsService.getSkillDetails(this.$route.params.projectId, this.$route.params.subjectId, this.$route.params.skillId)
          .then((response) => {
            this.skill = Object.assign(response, { subjectId: this.$route.params.subjectId });
            if (!this.skill.sharedToCatalog) {
              this.initData();
            } else {
              this.loading.finishedAllSkills = true;
              this.loading.finishedDependents = true;
            }
          });
      },
      deleteSkill(deleteItem) {
        const msg = `Are you sure you want to remove "${deleteItem.name}""?`;
        this.msgConfirm(msg, 'WARNING', 'Yes, Please!').then((res) => {
          if (res) {
            this.skillDeleted(deleteItem);
          }
        });
      },
      skillDeleted(deletedItem) {
        this.loading.finishedDependents = false;
        SkillsService.removeDependency(this.$route.params.projectId, this.$route.params.skillId, deletedItem.skillId, deletedItem.projectId)
          .then(() => {
            this.errNotification.enable = false;
            this.loadDependentSkills();
          });
      },
      skillAdded(newItem) {
        this.loading.finishedDependents = false;
        SkillsService.assignDependency(this.$route.params.projectId, this.$route.params.skillId, newItem.skillId, newItem.otherProjectId)
          .then(() => {
            this.errNotification.enable = false;
            this.loadDependentSkills();
            SkillsReporter.reportSkill('CreateSkillDependencies');
            if (newItem.otherProjectId && this.$route.params.projectId !== newItem.otherProjectId) {
              SkillsReporter.reportSkill('CreateCrossProjectSkillDependencies');
            }
          })
          .catch((e) => {
            if (e.response.data && e.response.data.errorCode && e.response.data.errorCode === 'FailedToAssignDependency') {
              this.errNotification.msg = e.response.data.explanation;
              this.errNotification.enable = true;
              this.loading.finishedDependents = true;

              // force reactivity on children - copy the data trick
              // specifically so the select component removes failed item from its list
              this.skills = this.skills.map((entry) => entry);
            } else if (e?.response?.data?.errorCode === 'DbUpgradeInProgress') {
              this.$router.push({ name: 'DbUpgradeInProgressPage' });
            } else {
              const errorMessage = (e.response && e.response.data && e.response.data.message) ? e.response.data.message : undefined;
              this.$router.push({ name: 'ErrorPage', query: { errorMessage } });
            }
          });
      },
      loadDependentSkills() {
        this.loading.finishedDependents = false;
        SkillsService.getDependentSkillsGraphForSkill(this.$route.params.projectId, this.$route.params.skillId)
          .then((response) => {
            this.graph = Object.assign(response, { subjectId: this.$route.params.subjectId });
            if (this.graph.nodes && this.graph.nodes.length > 0) {
              const mySkill = this.graph.nodes.find((entry) => entry.skillId === this.$route.params.skillId && entry.projectId === this.$route.params.projectId);
              this.skill.id = mySkill.id;
              const myEdges = this.graph.edges.filter((entry) => entry.fromId === mySkill.id);
              const myChildren = this.graph.nodes.filter((item) => myEdges.find((item1) => item1.toId === item.id));
              this.skills = myChildren.map((entry) => {
                const externalProject = entry.projectId !== this.skill.projectId;
                const disableInfo = {
                  manageBtn: {
                    disable: externalProject,
                    msg: 'Cannot manage skills from external projects.',
                  },
                };
                return Object.assign(entry, {
                  disabledStatus: disableInfo,
                  isFromAnotherProject: externalProject,
                });
              });
            } else {
              this.skills = [];
            }
          })
          .finally(() => {
            this.loading.finishedDependents = true;
          });
      },
      loadAllSkills() {
        this.loading.finishedAllSkills = false;
        SkillsService.getSkillsFroDependency(this.$route.params.projectId)
          .then((skills) => {
            this.allSkills = skills.filter((item) => (item.skillId !== this.$route.params.skillId || item.otherProjectId));
            this.loading.finishedAllSkills = true;
          })
          .finally(() => {
            this.loading.finishedAllSkills = true;
          });
      },
    },
  };
</script>

<style scoped>
  .dependencies-container {
    min-height: 800px;
  }

  .vs__dropdown-option--highlight .skills-option-id {
    color: #FFFFFF !important;
  }
</style>
