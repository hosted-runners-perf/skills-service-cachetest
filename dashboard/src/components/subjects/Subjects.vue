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
  <div ref="mainFocus">
    <loading-container v-bind:is-loading="isLoading">
      <sub-page-header ref="subPageHeader" title="Subjects" :action="isReadOnlyProj ? null : 'Subject'" @add-action="openNewSubjectModal"
                       :disabled="addSubjectDisabled" :disabled-msg="addSubjectsDisabledMsg"
                       :aria-label="'new subject'"/>
      <jump-to-skill />
      <div v-if="subjects && subjects.length" class="row justify-content-center" id="subjectCards" data-cy="subjectCards">
          <div v-for="(subject) of subjects" :key="subject.subjectId" :id="subject.subjectId" class="col-lg-4 mb-3"
               style="min-width: 23rem;" :data-cy="`${subject.subjectId}_card`">
            <div class="h-100">
              <b-overlay :show="sortOrder.loading" rounded="sm" opacity="0.4" class="h-100">
                <template #overlay>
                  <div class="text-center" :data-cy="`${subject.subjectId}_overlayShown`">
                    <div v-if="subject.subjectId===sortOrder.loadingSubjectId" data-cy="updatingSortMsg">
                      <div class="text-info text-uppercase mb-1">Updating sort order!</div>
                      <b-spinner label="Loading..." style="width: 3rem; height: 3rem;" variant="info"/>
                    </div>
                  </div>
                </template>

                <subject :subject="subject"
                         :ref="`subj${subject.subjectId}`"
                         @subject-deleted="deleteSubject"
                         @sort-changed-requested="updateSortAndReloadSubjects"
                         :disable-sort-control="subjects.length === 1"/>
              </b-overlay>
            </div>
          </div>
      </div>

      <no-content2 v-else class="mt-4"
                   title="No Subjects Yet" message="Subjects are a way to group and organize skill definitions within a gameified training profile."></no-content2>
    </loading-container>

    <edit-subject v-if="displayNewSubjectModal" v-model="displayNewSubjectModal"
                  :subject="emptyNewSubject" @subject-saved="subjectAdded"
                  @hidden="handleHide"/>
  </div>
</template>

<script>
  import Sortable from 'sortablejs';
  import { createNamespacedHelpers } from 'vuex';
  import { SkillsReporter } from '@skilltree/skills-client-vue';
  import ProjConfigMixin from '@/components/projects/ProjConfigMixin';
  import Subject from './Subject';
  import EditSubject from './EditSubject';
  import LoadingContainer from '../utils/LoadingContainer';
  import SubjectsService from './SubjectsService';
  import SubPageHeader from '../utils/pages/SubPageHeader';
  import NoContent2 from '../utils/NoContent2';
  import JumpToSkill from './JumpToSkill';

  const projects = createNamespacedHelpers('projects');
  const subjectsStore = createNamespacedHelpers('subjects');

  export default {
    name: 'Subjects',
    mixins: [ProjConfigMixin],
    components: {
      JumpToSkill,
      NoContent2,
      EditSubject,
      SubPageHeader,
      LoadingContainer,
      Subject,
    },
    data() {
      return {
        isLoadingData: true,
        displayNewSubjectModal: false,
        projectId: null,
        sortOrder: {
          loading: false,
          loadingSubjectId: '-1',
        },
      };
    },
    mounted() {
      this.projectId = this.$route.params.projectId;
      this.doLoadSubjects();
    },
    watch: {
      '$route.params.projectId': function projectIdParamUpdated() {
        this.projectId = this.$route.params.projectId;
        this.doLoadSubjects();
      },
    },
    methods: {
      ...projects.mapActions([
        'loadProjectDetailsState',
      ]),
      ...subjectsStore.mapActions([
        'loadSubjects',
      ]),
      openNewSubjectModal() {
        this.displayNewSubjectModal = true;
      },
      doLoadSubjects() {
        return this.loadSubjects({ projectId: this.$route.params.projectId })
          .finally(() => {
            this.isLoadingData = false;
            this.enableDropAndDrop();
          });
      },
      deleteSubject(subject) {
        this.isLoadingData = true;
        SubjectsService.deleteSubject(subject)
          .then(() => {
            this.loadProjectDetailsState({ projectId: this.projectId });
            this.loadSubjects({ projectId: this.$route.params.projectId })
              .then(() => {
                this.isLoadingData = false;
                this.$emit('subjects-changed', subject.subjectId);
                this.$nextTick(() => {
                  this.$announcer.polite(`Subject ${subject.name} has been deleted`);
                });
              });
          });
      },
      updateSortAndReloadSubjects(updateInfo) {
        const sortedSubjects = this.subjects.sort((a, b) => {
          if (a.displayOrder > b.displayOrder) {
            return 1;
          }
          if (b.displayOrder > a.displayOrder) {
            return -1;
          }
          return 0;
        });
        const currentIndex = sortedSubjects.findIndex((item) => item.subjectId === updateInfo.id);
        const newIndex = updateInfo.direction === 'up' ? currentIndex - 1 : currentIndex + 1;
        if (newIndex >= 0 && (newIndex) < this.subjects.length) {
          this.isLoadingData = true;
          const { projectId } = this.$route.params;
          SubjectsService.updateSubjectsDisplaySortOrder(projectId, updateInfo.id, newIndex)
            .finally(() => {
              this.doLoadSubjects()
                .then(() => {
                  this.isLoadingData = false;
                  const foundRef = this.$refs[`subj${updateInfo.id}`];
                  this.$nextTick(() => {
                    foundRef[0].focusSortControl();
                  });
                });
            });
        }
      },
      subjectAdded(subject) {
        this.displayNewSubjectModal = false;
        this.isLoadingData = true;
        SubjectsService.saveSubject(subject)
          .then(() => {
            this.doLoadSubjects()
              .then(() => {
              this.handleFocus().then(() => {
                this.$nextTick(() => {
                  this.$announcer.polite(`Subject ${subject.name} has been saved`);
                });
              });
            });
            this.loadProjectDetailsState({ projectId: this.projectId });
            this.$emit('subjects-changed', subject.subjectId);
            SkillsReporter.reportSkill('CreateSubject');
          });
      },
      handleHide(e) {
        if (!e || !e.update) {
          this.handleFocus();
        }
      },
      handleFocus() {
        return new Promise((resolve) => {
          this.$nextTick(() => {
            this.$refs?.subPageHeader?.$refs?.actionButton?.focus();
            resolve();
          });
        });
      },
      enableDropAndDrop() {
        if (this.subjects && this.subjects.length > 0) {
          const self = this;
          this.$nextTick(() => {
            const cards = document.getElementById('subjectCards');
            Sortable.create(cards, {
              handle: '.sort-control',
              animation: 150,
              ghostClass: 'skills-sort-order-ghost-class',
              onUpdate(event) {
                self.sortOrderUpdate(event);
              },
            });
          });
        }
      },
      sortOrderUpdate(updateEvent) {
        const { id } = updateEvent.item;
        this.sortOrder.loadingSubjectId = id;
        this.sortOrder.loading = true;
        SubjectsService.updateSubjectsDisplaySortOrder(this.projectId, id, updateEvent.newIndex)
          .finally(() => {
            this.sortOrder.loading = false;
          });
      },
    },
    computed: {
      ...subjectsStore.mapGetters([
        'subjects',
      ]),
      isLoading() {
        return this.isLoadingData || this.isLoadingProjConfig;
      },
      emptyNewSubject() {
        return {
          projectId: this.$route.params.projectId,
          name: '',
          subjectId: '',
          description: '',
          iconClass: 'fas fa-book',
        };
      },
      addSubjectDisabled() {
        return this.subjects && this.$store.getters.config && this.subjects.length >= this.$store.getters.config.maxSubjectsPerProject;
      },
      addSubjectsDisabledMsg() {
        if (this.$store.getters.config) {
          return `The maximum number of Subjects allowed is ${this.$store.getters.config.maxSubjectsPerProject}`;
        }
        return '';
      },
    },
  };
</script>

<style scoped>

  .no-subjects {
    color: #3f5971;
  }

</style>
