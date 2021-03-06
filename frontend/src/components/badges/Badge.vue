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
  <page-preview-card :options="cardOptions">
    <div slot="header-top-right">
      <edit-and-delete-dropdown v-on:deleted="deleteBadge" v-on:edited="showEditBadge=true" v-on:move-up="moveUp"
                                v-on:move-down="moveDown"
                                :isFirst="badgeInternal.isFirst" :isLast="badgeInternal.isLast" :isLoading="isLoading"
                                class="badge-settings"></edit-and-delete-dropdown>
    </div>
    <div slot="footer">
      <i v-if="badgeInternal.endDate" class="fas fa-gem position-absolute" style="font-size: 1rem; top: 1rem; left: 1rem; color: purple"></i>
      <router-link :to="buildManageLink()"
                   class="btn btn-outline-primary btn-sm">
        Manage <i class="fas fa-arrow-circle-right"/>
      </router-link>

      <edit-badge v-if="showEditBadge" v-model="showEditBadge" :id="badge.badgeId" :badge="badge" :is-edit="true"
                  :global="global" @badge-updated="badgeEdited"></edit-badge>
    </div>
  </page-preview-card>
</template>

<script>
  import EditAndDeleteDropdown from '@/components/utils/EditAndDeleteDropdown';
  import EditBadge from './EditBadge';
  import MsgBoxMixin from '../utils/modal/MsgBoxMixin';
  import PagePreviewCard from '../utils/pages/PagePreviewCard';

  export default {
    name: 'Badge',
    components: { PagePreviewCard, EditAndDeleteDropdown, EditBadge },
    props: {
      badge: Object,
      global: {
        type: Boolean,
        default: false,
      },
    },
    mixins: [MsgBoxMixin],
    data() {
      return {
        isLoading: false,
        badgeInternal: Object.assign({}, this.badge),
        cardOptions: {},
        showEditBadge: false,
      };
    },
    watch: {
      badge: function badgeWatch(newBadge, oldBadge) {
        if (oldBadge) {
          this.badgeInternal = newBadge;
          this.buildCardOptions();
        }
      },
    },
    mounted() {
      this.buildCardOptions();
    },
    methods: {
      buildCardOptions() {
        const stats = [{
          label: 'Number Skills',
          count: this.badgeInternal.numSkills,
        }];
        if (!this.global) {
          stats.push({
            label: 'Total Points',
            count: this.badgeInternal.totalPoints,
          });
        } else {
          stats.push({
            label: 'Total Projects',
            count: this.badgeInternal.uniqueProjectCount,
          });
        }
        this.cardOptions = {
          icon: this.badgeInternal.iconClass,
          title: this.badgeInternal.name,
          subTitle: `ID: ${this.badgeInternal.badgeId}`,
          stats,
        };
      },
      buildManageLink() {
        const link = {
          name: this.global ? 'GlobalBadgeSkills' : 'BadgeSkills',
          params: {
            projectId: this.badgeInternal.projectId,
            badgeId: this.badgeInternal.badgeId,
            badge: this.badgeInternal,
          },
        };
        return link;
      },
      deleteBadge() {
        const msg = `Deleting Badge Id: ${this.badgeInternal.badgeId} this cannot be undone.`;
        this.msgConfirm(msg, 'WARNING: Delete Badge').then((res) => {
          if (res) {
            this.badgeDeleted();
          }
        });
      },
      badgeEdited(badge) {
        this.$emit('badge-updated', badge);
      },
      badgeDeleted() {
        this.$emit('badge-deleted', this.badgeInternal);
      },
      moveUp() {
        this.$emit('move-badge-up', this.badgeInternal);
      },
      moveDown() {
        this.$emit('move-badge-down', this.badgeInternal);
      },
    },
  };
</script>

<style scoped>
  .badge-settings {
    position: relative;
    display: inline-block;
    float: right;
  }

  .badge-title {
    display: inline-block;
  }

  .badge-icon {
    font-size: 2rem;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 5px;
    box-shadow: 0 22px 35px -16px rgba(0, 0, 0, 0.1);
  }
</style>
