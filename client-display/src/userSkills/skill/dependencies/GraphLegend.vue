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
  <div class='graph-legend card skills-card-theme-border' style="min-width: 13rem;">
    <div class="card-header">
      <h6 class="card-title mb-0 float-left">Node Legend</h6>
    </div>
    <div class='card-body'>
      <ul class='text-left p-0 mb-0' data-cy="nodeLegend">
        <li v-for="item in items" v-bind:key="item.label" class="legend-list-item mb-0">
          <span v-bind:style=' {background: item.color }' class="mr-2 mb-0 border border-secondary rounded"></span>{{ item.label }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'GraphLegend',
    props: ['items'],
  };
</script>

<style scoped>
  .legend-list-item {
    font-size: 0.8rem;
    list-style: none;
  }

  .legend-list-item span {
    display: block;
    float: left;
    height: 16px;
    width: 30px;
  }

</style>
