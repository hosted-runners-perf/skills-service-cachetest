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
  <div class="row bg-white border-bottom py-2 mb-3 text-center" data-cy="subPageHeader">
    <div class="col-sm-6 col-md-7 text-sm-left">
      <h1 class="h4 text-uppercase">{{ title }}</h1>
    </div>
    <div class="col-sm-6 col-md-5 pt-0 text-sm-right" data-cy="subPageHeaderControls">
      <div v-if="!isLoading">
        <slot v-if="!isReadOnlyProjUnderAdminUrl">
          <b-button ref="actionButton" v-if="action" type="button" size="sm" variant="outline-primary"
                    :class="{'btn':true, 'btn-outline-primary':true, 'disabled':disabledInternal}"
                    v-on:click="addClicked" :aria-label="ariaLabel ? ariaLabel : action"
                    :data-cy="`btn_${title}`">
            <span class="">{{ action }} </span> <i class="fas fa-plus-circle"/>
          </b-button>
          <i v-if="disabledInternal" class="fas fa-exclamation-circle text-warning ml-1"
             style="pointer-events: all; font-size: 1.5rem;"
             :aria-label="disabledMsg"
             v-b-tooltip.hover="disabledMsg"/>
        </slot>
      </div>
    </div>
  </div>
</template>

<script>
  import ProjConfigMixin from '@/components/projects/ProjConfigMixin';

  export default {
    name: 'SubPageHeader',
    mixins: [ProjConfigMixin],
    props: {
      title: {
        type: String,
        required: true,
      },
      action: {
        type: String,
        required: false,
      },
      disabled: {
        type: Boolean,
        required: false,
        default: false,
      },
      disabledMsg: {
        type: String,
        required: false,
      },
      ariaLabel: {
        type: String,
        required: false,
      },
      isLoading: {
        type: Boolean,
        required: false,
        default: false,
      },
    },
    data() {
      return {
        disabledInternal: this.disabled,
      };
    },
    watch: {
      disabled() {
        this.disabledInternal = this.disabled;
      },
    },
    computed: {
      isReadOnlyProjUnderAdminUrl() {
        return this.isReadOnlyProj && this.$route.path?.toLowerCase()?.startsWith('/administrator');
      },
    },
    methods: {
      addClicked() {
        this.$emit('add-action');
      },
    },
  };
</script>

<style scoped>

</style>
