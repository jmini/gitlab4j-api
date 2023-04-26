
package org.gitlab4j.api.models;

import org.gitlab4j.api.utils.JacksonJson;

public class Issue extends AbstractIssue {

    /**
     * @deprecated this value is only available in {@link LinkedIssue}
     */
    @Deprecated
    private Long issueLinkId;
    private Boolean subscribed;

    /**
     * @deprecated use {@link LinkedIssue#getIssueLinkId()}
     */
    @Deprecated
    public Long getIssueLinkId() {
        return issueLinkId;
    }

    /**
     * @deprecated use {@link LinkedIssue#setIssueLinkId(Long)}
     */
    @Deprecated
    public void setIssueLinkId(Long issueLinkId) {
        this.issueLinkId = issueLinkId;
    }

    public Boolean getSubscribed() {
        return subscribed;
    }

    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}
