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
    <div class="card">
        <div class="card-body">
            <div class="media">
                <div class="d-inline-block mt-2 mr-3 border p-3 rounded-circle">
                    <i class="fa-3x" :class="icon" style="min-width: 3.5rem;"/>
                </div>
                <div class="media-body text-left">
                    <slot/>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        name: 'MyRankEncouragementCard',
        props: {
            icon: String,
        },
    };
</script>

<style scoped>

</style>
