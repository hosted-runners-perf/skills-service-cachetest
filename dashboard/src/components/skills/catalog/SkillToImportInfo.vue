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
  <b-card :data-cy="`skillToImportInfo-${skill.projectId}_${skill.skillId}`">
    <div class="row">
      <div class="col">
        <i class="fas fa-laptop skills-color-selfreport"></i> <span class="font-italic">Self Report:</span> <span class="text-primary">{{ selfReport }}</span>
      </div>
      <div class="col-auto">
        <i class="fas fa-book text-info"></i> <span class="font-italic">Exported:</span> <span class="text-primary">{{ skill.exportedOn | date }}</span> <span class="text-secondary">({{ skill.exportedOn | timeFromNow }})</span>
      </div>
    </div>
    <div class="row mt-1">
      <div class="col">
        <div>
          <span class="font-italic">Project ID: </span><span class="text-primary font-weight-bold" data-cy="projId">{{ skill.projectId }}</span>
        </div>
        <div>
          <span class="font-italic">Skill ID: </span><span class="text-primary font-weight-bold" data-cy="skillId"><show-more :limit="50" :text="skill.skillId" /></span>
        </div>
        <div>
          <span class="font-italic">Points: </span><span class="text-primary font-weight-bold" data-cy="totalPts">{{ skill.totalPoints}}</span><span> ({{ skill.pointIncrement }} Increment x {{ skill.numPerformToCompletion }} Occurrences)</span>
        </div>
      </div>
      <div class="col-auto">
      </div>
    </div>
    <div class="card mt-3">
      <div class="card-header">
        Description
      </div>
      <div class="card-body">
        <markdown-text v-if="skill.description" :text="skill.description" data-cy="importedSkillInfoDescription"/>
        <p v-else class="text-muted">
          Not Specified
        </p>
      </div>
    </div>
  </b-card>
</template>

<script>
  import MarkdownText from '@/common-components/utilities/MarkdownText';
  import ShowMore from '@/components/skills/selfReport/ShowMore';

  export default {
    name: 'SkillToImportInfo',
    components: { MarkdownText, ShowMore },
    props: {
      skill: Object,
    },
    computed: {
      selfReport() {
        if (!this.skill.selfReportingType) {
          return 'N/A';
        }

        return (this.skill.selfReportingType === 'Approval') ? 'Requires Approval' : 'Honor System';
      },
    },
  };
</script>

<style scoped>

</style>
