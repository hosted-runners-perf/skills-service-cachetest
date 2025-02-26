/*
 * Copyright 2020 SkillTree
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import VueApexCharts from 'vue-apexcharts';

import Vue from 'vue';
import {
    ModalPlugin,
    DropdownPlugin,
    FormInputPlugin,
    ButtonPlugin,
    TablePlugin,
    BadgePlugin,
    ProgressPlugin,
    SpinnerPlugin,
    TooltipPlugin,
    FormTextareaPlugin,
} from 'bootstrap-vue';
import FiltersPlugin from '@/common-components/filter/FiltersPlugin';
import App from '@/App';
import router from '@/router';
import store from '@/store/store';
import 'apexcharts';
import '@/common/filter/DayJsFilters';
import DevModeUtil from '@/dev/DevModeUtil';

Vue.config.productionTip = false;

Vue.use(VueApexCharts);
Vue.use(ModalPlugin);
Vue.use(DropdownPlugin);
Vue.use(FormInputPlugin);
Vue.use(ButtonPlugin);
Vue.use(TablePlugin);
Vue.use(BadgePlugin);
Vue.use(ProgressPlugin);
Vue.use(SpinnerPlugin);
Vue.use(TooltipPlugin);
Vue.use(FormTextareaPlugin);
Vue.use(FiltersPlugin);

require('@/common/interceptors/softwareVersionInterceptor');
require('@/common/interceptors/upgradeInProgressInterceptor');

const initializeVueApp = () => {
  new Vue({
    router,
    store,
    render: (h) => h(App),
  }).$mount('#app');
};

if (DevModeUtil.isDevelopmentMode()) {
  DevModeUtil.configureDevelopmentMode(store).then(() => { initializeVueApp(); });
} else {
  initializeVueApp();
}
