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
  <ValidationObserver ref="observer" v-slot="{invalid, handleSubmit}" slim>
    <b-modal :id="internalGroup.skillId"
             :title="title"
             @hide="publishHidden"
             v-model="show"
             size="xl"
             :no-close-on-backdrop="true"
             :centered="true"
             data-cy="EditSkillGroupModal"
             header-bg-variant="info"
             header-text-variant="light" no-fade>
      <skills-spinner :is-loading="isLoading" />
      <b-container :is-loading="!isLoading" fluid>
        <div class="row">
          <div class="col-12">
            <div class="form-group">
              <label for="groupNameInput">* Group Name</label>
              <ValidationProvider rules="required|minNameLength|maxSkillNameLength|uniqueGroupName|customNameValidator"
                                  v-slot="{errors}"
                                  name="Group Name">
                <input class="form-control" type="text" v-model="internalGroup.name"
                       v-on:input="updateId"
                       v-on:keydown.enter="handleSubmit(updateGroup)"
                       v-focus
                       data-cy="groupName"
                       id="groupNameInput"
                       :aria-invalid="errors && errors.length > 0"
                       aria-errormessage="groupNameError"
                       aria-describedby="groupNameError"/>
                <small role="alert" class="form-text text-danger" data-cy="groupNameError" id="groupNameError">{{ errors[0] }}</small>
              </ValidationProvider>
            </div>
          </div>

          <div class="col-12">
            <id-input type="text" label="Group ID" v-model="internalGroup.skillId"
                      additional-validation-rules="uniqueGroupId" @can-edit="canEditGroupId=$event"
                      v-on:keydown.enter.native="handleSubmit(updateGroup)"
                      :next-focus-el="previousFocus"
                      @shown="tooltipShowing=true"
                      @hidden="tooltipShowing=false"/>
          </div>
        </div>

        <div class="mt-3">
          <label class="label">Description</label>
          <div class="control">
            <ValidationProvider rules="maxDescriptionLength|customDescriptionValidator" :debounce="250" v-slot="{errors}" name="Group Description">
              <markdown-editor v-if="internalGroup && (!isEdit || !isLoading)"
                               v-model="internalGroup.description"
                               :aria-invalid="errors && errors.length > 0"
                               aria-errormessage="groupDescriptionError"
                               aria-describedby="groupDescriptionError"
                               data-cy="groupDescription"/>
              <small role="alert" id="groupDescriptionError" class="form-text text-danger" data-cy="groupDescriptionError">{{ errors[0] }}</small>
            </ValidationProvider>
          </div>
        </div>

        <p v-if="invalid && overallErrMsg" class="text-center text-danger mt-2" aria-live="polite"><small>***{{ overallErrMsg }}***</small></p>
      </b-container>

      <div slot="modal-footer" class="w-100">
        <b-button variant="success" size="sm" class="float-right" @click="handleSubmit(updateGroup)"
                  :disabled="invalid"
                  data-cy="saveGroupButton">
          Save
        </b-button>
        <b-button variant="secondary" size="sm" class="float-right mr-2" @click="close" data-cy="closeGroupButton">
          Cancel
        </b-button>
      </div>
    </b-modal>
  </ValidationObserver>
</template>

<script>
  import { extend } from 'vee-validate';
  import IdInput from '../../utils/inputForm/IdInput';
  import InputSanitizer from '../../utils/InputSanitizer';
  import SkillsService from '../SkillsService';
  import MarkdownEditor from '../../utils/MarkdownEditor';
  import SkillsSpinner from '../../utils/SkillsSpinner';

  export default {
    name: 'EditSkillGroup',
    components: { SkillsSpinner, MarkdownEditor, IdInput },
    props: {
      group: Object,
      isEdit: Boolean,
      value: {
        type: Boolean,
        required: true,
      },
    },
    data() {
      return {
        isLoading: this.isEdit,
        show: this.value,
        internalGroup: {
          originalSkillId: this.group.skillId,
          isEdit: this.isEdit,
          description: null,
          ...this.group,
        },
        canEditGroupId: false,
        overallErrMsg: '',
        original: {
          name: '',
          skillId: '',
          projectId: '',
        },
        currentFocus: null,
        previousFocus: null,
        tooltipShowing: false,
      };
    },
    created() {
      this.registerValidation();
    },
    mounted() {
      this.original = {
        name: this.group.name,
        skillId: this.group.skillId,
        projectId: this.group.projectId,
      };
      if (this.isEdit) {
        this.isLoading = true;
        SkillsService.getSkillDetails(this.group.projectId, this.group.subjectId, this.group.skillId)
          .then((res) => {
            this.internalGroup.description = res.description;
          }).finally(() => {
            this.isLoading = false;
          });
      }
      document.addEventListener('focusin', this.trackFocus);
    },
    computed: {
      title() {
        return this.isEdit ? 'Editing Existing Skills Group' : 'New Skills Group';
      },
    },
    watch: {
      show(newValue) {
        this.$emit('input', newValue);
      },
    },
    methods: {
      trackFocus() {
        this.previousFocus = this.currentFocus;
        this.currentFocus = document.activeElement;
      },
      handleIdToggle(canEdit) {
        this.canEditGroupId = canEdit;
      },
      close() {
        this.show = false;
        this.publishHidden({});
      },
      updateGroup() {
        this.$refs.observer.validate()
          .then((res) => {
            if (!res) {
              this.overallErrMsg = 'Form did NOT pass validation, please fix and try to Save again';
            } else {
              this.internalGroup.name = InputSanitizer.sanitize(this.internalGroup.name);
              this.internalGroup.projectId = InputSanitizer.sanitize(this.internalGroup.projectId);
              this.$emit('group-saved', this.internalGroup);
              this.close();
            }
          });
      },
      updateId() {
        if (!this.isEdit && !this.canEditGroupId) {
          let id = InputSanitizer.removeSpecialChars(this.internalGroup.name);
          if (id) {
            id = `${id}Group`;
          }
          this.internalGroup.skillId = id;
        }
      },
      publishHidden(e) {
        if (this.tooltipShowing) {
          e.preventDefault();
        } else {
          this.$emit('hidden', e);
        }
      },
      registerValidation() {
        const self = this;
        extend('uniqueGroupName', {
          message: (field) => `The value for the ${field} is already taken.`,
          validate(value) {
            if (self.isEdit && (self.original.name === value || self.original.name.localeCompare(value, 'en', { sensitivity: 'base' }) === 0)) {
              return true;
            }
            return SkillsService.skillWithNameExists(self.original.projectId, value);
          },
        });

        extend('uniqueGroupId', {
          message: (field) => `The value for the ${field} is already taken.`,
          validate(value) {
            if (self.isEdit && self.original.skillId === value) {
              return true;
            }
            return SkillsService.skillWithIdExists(self.original.projectId, value);
          },
        });
      },
    },
  };
</script>

<style lang="scss" scoped>

</style>
