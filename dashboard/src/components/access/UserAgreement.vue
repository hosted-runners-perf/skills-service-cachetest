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
  <loading-container :is-loading="loading" class="container-fluid">
    <div class="row justify-content-center text-center">
      <div class="mt-3">
        <div class="mt-5">
          <logo1 />
        </div>

        <b-row class="mt-3">
          <b-col>
            <div class="card p-0 m-0">
              <div class="card-body mt-2 mb-0 p-0">
                <div class="h4 text-uppercase text-center">User Agreement</div>
              </div>
            </div>
          </b-col>
        </b-row>

        <div v-if="!loading" class="text-left overflow-auto m-2 m-md-5 p-2 p-md-5 pb-xs-5 border" style="min-width: 60%">
          <markdown-text data-cy="userAgreement" :text="userAgreement"/>
          <div class="btn-toolbar" role="toolbar">
            <button class="btn mr-2 btn-outline-danger" type="button" v-on:click="signOut"
                    data-cy="rejectUserAgreement">
              No Thanks
              <i :class="[isSaving ? 'fa fa-circle-notch fa-spin fa-3x-fa-fw' : 'fas fa-ban']"></i>
            </button>
            <button class="btn mr-2 btn-outline-success" type="button" v-on:click="acknowledgeUa"
                    data-cy="acknowledgeUserAgreement">
              I Agree
              <i :class="[isSaving ? 'fa fa-circle-notch fa-spin fa-3x-fa-fw' : 'fas fa-arrow-circle-right']"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  </loading-container>
</template>

<script>
  import MarkdownText from '@/common-components/utilities/MarkdownText';
  import AccessService from './AccessService';
  import SettingsService from '../settings/SettingsService';
  import LoadingContainer from '../utils/LoadingContainer';
  import NavigationErrorMixin from '../utils/NavigationErrorMixin';
  import Logo1 from '../brand/Logo1';

  export default {
    name: 'UserAgreement',
    components: { MarkdownText, LoadingContainer, Logo1 },
    mixins: [NavigationErrorMixin],
    data() {
      return {
        userAgreement: '',
        uaVersion: '',
        loading: true,
        isSaving: false,
      };
    },
    mounted() {
      AccessService.getUserAgreement().then((ua) => {
        if (ua) {
          this.userAgreement = ua.userAgreement;
          this.uaVersion = ua.currentVersion;
        }
      }).finally(() => {
        this.loading = false;
      });
    },
    methods: {
      acknowledgeUa() {
        this.isSaving = true;
        const ack = {
          settingGroup: 'user',
          setting: 'viewed_user_agreement',
          value: this.uaVersion,
        };
        SettingsService.saveUserSettings([ack]).then(() => {
          // redirect to original page
          this.$store.commit('showUa', false);
          this.handlePush(this.$route.query.redirect || '/');
        }).finally(() => {
          this.isSaving = false;
        });
      },
      signOut() {
        this.$store.dispatch('logout');
      },
    },
  };
</script>

<style scoped>

</style>
