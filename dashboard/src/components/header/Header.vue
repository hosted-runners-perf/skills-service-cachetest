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
  <div class="bg-white header">
    <a
      class="skip-main btn btn-primary"
      @click="focusOnMainContent"
      @keydown.prevent.enter="focusOnMainContent"
      tabindex="0"
      data-cy="skipToContentButton">Skip to content</a>
    <div v-if="isUpgradeInProgress" class="container-fluid p-3 text-center bg-warning mb-1" data-cy="upgradeInProgressWarning">
      <span class="fa-stack fa-2x" style="vertical-align: middle; font-size:1em;">
        <i class="fas fa-circle fa-stack-2x"></i>
        <i class="fas fa-hammer fa-stack-1x fa-inverse"></i>
      </span>
      <span class="pl-1">An upgrade is currently in process. Please note that no changes will be permitted until the upgrade is complete.
      Any reported skills will be queued for application once the upgrade has completed.</span>
    </div>
    <div class="container-fluid py-3">
      <div class="row">
        <div class="col-sm">
          <div class="text-center text-sm-left">
            <router-link data-cy="skillTreeLogo" class="h2 text-primary ml-2" to="/">
              <img ref="skillTreeLogo" src="/static/img/skilltree_logo_v1.png" alt="skilltree logo"/>
            </router-link>
            <span v-if="isAdminPage" ref="adminStamp" class="skills-stamp">ADMIN</span>
          </div>
        </div>

        <hr class="w-75 mb-0 d-sm-none"/>

        <div class="col-sm-auto text-center text-sm-right pt-sm-2 mt-3 mt-sm-0">
          <inception-button v-if="isAdminPage" class="mr-2" data-cy="inception-button"></inception-button>
<!--          message center is just mocked up for now and will be implemented in its own ticket -->
<!--          <message-center />-->
          <settings-button data-cy="settings-button"/>
          <help-button class="" data-cy="help-button"/>
        </div>
      </div>
    </div>
    <breadcrumb></breadcrumb>
  </div>
</template>

<script>
  import store from '@/store/store';
  import SettingsButton from './SettingsButton';
  import Breadcrumb from './Breadcrumb';
  import InceptionButton from '../inception/InceptionButton';
  import HelpButton from './HelpButton';

  export default {
    name: 'Header',
    components: {
      HelpButton,
      InceptionButton,
      Breadcrumb,
      SettingsButton,
    },
    computed: {
      isAdminPage() {
        return this.$route && this.$route.meta && this.$route.meta.requiresAuth && !this.$route.meta.nonAdmin;
      },
      isUpgradeInProgress() {
        return store.getters.config.dbUpgradeInProgress === 'true';
      },
    },
    beforeDestroy() {
      window.removeEventListener('resize', this.updateAdminStampSize);
    },
    methods: {
      focusOnMainContent() {
        const toSearch = ['mainContent3', 'mainContent2', 'mainContent1'];
        //  there are currently only 3 levels
        this.$nextTick(() => {
          const foundId = toSearch.find((id) => document.getElementById(id));
          if (foundId) {
            this.$nextTick(() => {
              const focusOn = document.getElementById(foundId);
              if (focusOn) {
                focusOn.focus({});
              }
            });
          }
        });
      },
    },
  };
</script>

<style scoped>
/* black-ops-one-regular - latin-ext_latin */
@font-face {
  font-family: 'Black Ops One';
  font-style: normal;
  font-weight: 400;
  src: url('../../fonts/black-ops-one-v12-latin-ext_latin-regular.eot'); /* IE9 Compat Modes */
  src: local(''),
  url('../../fonts/black-ops-one-v12-latin-ext_latin-regular.eot?#iefix') format('embedded-opentype'), /* IE6-IE8 */
  url('../../fonts/black-ops-one-v12-latin-ext_latin-regular.woff2') format('woff2'), /* Super Modern Browsers */
  url('../../fonts/black-ops-one-v12-latin-ext_latin-regular.woff') format('woff'), /* Modern Browsers */
  url('../../fonts/black-ops-one-v12-latin-ext_latin-regular.ttf') format('truetype'), /* Safari, Android, iOS */
  url('../../fonts/black-ops-one-v12-latin-ext_latin-regular.svg#BlackOpsOne') format('svg'); /* Legacy iOS */
}

.skills-stamp {
  margin-left: 0.5rem;

  box-shadow: 0 0 0 3px #8b6d6d, 0 0 0 2px #8b6d6d inset;
  color: #722b2b;
  border: 2px solid transparent;
  border-radius: 4px;
  display: inline-block;
  padding: 5px 2px;
  line-height: 22px;
  font-size: 24px;
  font-family: 'Black Ops One', cursive;
  text-transform: uppercase;
  text-align: center;
  opacity: 0.8;
  width: 155px;
  transform: rotate(-17deg);
}

@media (max-width: 675px) {
  .skills-stamp {
    max-width: 9rem;
    line-height: 12px;
    font-size: 14px;
    width: 85px;
  }
}

@media (max-width: 563px) {
  .skills-stamp {
    max-width: 9rem;
    line-height: 12px;
    font-size: 14px;
    width: 85px;
  }
}

.skip-main {
  position: absolute !important;
  overflow: hidden !important;;
  z-index: -999 !important;;
}

.skip-main:focus, .skip-main:active {
  left: 5px !important;;
  top: 5px !important;;
  font-size: 1.2em !important;;
  z-index: 999 !important;;
}

</style>
