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
  <div v-if="skill.copiedFromProjectId" class="alert alert-info" style="font-size: 1rem;" data-cy="catalogImportStatus">
    <i class="fas fa-exclamation-circle"></i>
    <span v-if="skill.selfReporting && skill.selfReporting.enabled">
      This {{ skillDisplayName.toLowerCase() }} is originally defined in <span class="font-weight-bold font-italic">{{ skill.copiedFromProjectName }}</span> and re-used in this {{ projectDisplayName.toLowerCase() }}!
      This {{ skillDisplayName.toLowerCase() }} can can be self-reported via the <span class="font-weight-bold">"I did it"</span> button below.
    </span>
    <span v-else>
      This {{ skillDisplayName.toLowerCase() }} is originally defined in <span class="font-weight-bold font-italic">{{ skill.copiedFromProjectName }}</span> and re-used in this {{ projectDisplayName.toLowerCase() }}!
      Navigate to <span class="font-weight-bold font-italic">{{ skill.copiedFromProjectName }}</span> {{ projectDisplayName.toLowerCase() }} to perform <span class="font-weight-bold font-italic">{{ skill.skill }}</span> {{ skillDisplayName.toLowerCase() }}.
    </span>
  </div>
</template>

<script>
  export default {
    name: 'CatalogImportStatus',
    props: {
      skill: Object,
    },
  };
</script>

<style scoped>

</style>
