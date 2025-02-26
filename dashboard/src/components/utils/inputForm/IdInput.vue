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
  <ValidationProvider :rules="rules" v-slot="{ errors }" :name="label" ref="idVp">
    <div class="form-group mt-0 mb-0">
      <div class="row">
        <div class="col">
          <label for="idInput">* {{ label }}</label>
        </div>
        <div class="col text-right" data-cy="idInputEnableControl">
          <i class="fas fa-question-circle mr-1 text-secondary"
             id="idInputHelp"
             aria-label="Enable ID input to override auto-generated value."
             role="alert"
             tabindex="0"
             @keydown.esc="handleEscape"/>

          <b-tooltip target="idInputHelp"
                     title="Enable to override auto-generated value."
                     placement="left"
                     @shown="tooltipShown"
                     @hidden="tooltipHidden"/>
          <b-link v-if="!canEdit" @click="toggle" aria-label="enable manual ID override" data-cy="enableIdInput">Enable</b-link>
          <span v-else>Enabled <i class="fa fa-check fa-sm text-muted"/></span>
        </div>
      </div>
      <input type="text" class="form-control" id="idInput" v-model="internalValue" :disabled="!canEdit"
              @input="dataChanged" aria-required="true"
              :aria-invalid="errors && errors.length > 0"
              aria-errormessage="idError"
              aria-describedby="idError"
             data-cy="idInputValue">
      <small role="alert" class="form-text text-danger" data-cy="idError" id="idError">{{ errors[0]}}</small>
    </div>
  </ValidationProvider>
</template>

<script>
  import { extend, ValidationProvider } from 'vee-validate';
  // eslint-disable-next-line camelcase
  import { alpha_num } from 'vee-validate/dist/rules';
  import debounce from 'lodash.debounce';

  extend('alpha_num', alpha_num);

  export default {
    name: 'IdInput',
    components: {
      ValidationProvider,
    },
    props: {
      label: String,
      value: String,
      isSkillId: {
        type: Boolean,
        default: false,
      },
      additionalValidationRules: [String],
      nextFocusEl: HTMLElement,
    },
    data() {
      return {
        rules: `required|minIdLength|maxIdLength|${this.isSkillId ? 'skill_id_validator' : 'id_validator'}`,
        canEdit: false,
        internalValue: this.value,
      };
    },
    mounted() {
      if (this.additionalValidationRules) {
        this.rules = `${this.rules}|${this.additionalValidationRules}`;
      }
    },
    methods: {
      toggle() {
        this.canEdit = !this.canEdit;
        this.$emit('can-edit', this.canEdit);
      },
      dataChanged() {
        this.$emit('input', this.internalValue);
      },
      tooltipShown(e) {
        this.$emit('shown', e);
      },
      tooltipHidden(e) {
        this.$emit('hidden', e);
      },
      handleEscape() {
        document.activeElement.blur();
        this.nextFocusEl?.focus();
      },
      validateOnChange: debounce(function validate(val) {
        if (this.$refs.idVp) {
          this.$refs.idVp.syncValue(val);
          this.$refs.idVp.validate().then((validationResult) => {
            if (this.$refs.idVp) {
              this.$refs.idVp.applyResult(validationResult);
            }
          });
        }
      }, 200),
    },
    watch: {
      value(newVal) {
        this.internalValue = newVal;
      },
      internalValue(newVal) {
        this.validateOnChange(newVal);
      },
    },
  };
</script>

<style scoped>

</style>
