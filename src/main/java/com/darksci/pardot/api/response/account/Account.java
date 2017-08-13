package com.darksci.pardot.api.response.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.joda.time.DateTime;

/**
 * Represents a Pardot Account.
 */
public class Account {
    private Long id;
    private String company;
    private String level;
    private String website;
    private String vanityDomain;
    private Long pluginCampaignId;
    private String trackingCodeTemplate;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String territory;
    private Integer zip;
    private String country;
    private String phone;
    private String fax;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private DateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private DateTime updatedAt;

    public Long getId() {
        return id;
    }

    public String getCompany() {
        return company;
    }

    public String getLevel() {
        return level;
    }

    public String getWebsite() {
        return website;
    }

    public String getVanityDomain() {
        return vanityDomain;
    }

    public Long getPluginCampaignId() {
        return pluginCampaignId;
    }

    public String getTrackingCodeTemplate() {
        return trackingCodeTemplate;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getTerritory() {
        return territory;
    }

    public Integer getZip() {
        return zip;
    }

    public String getCountry() {
        return country;
    }

    public String getPhone() {
        return phone;
    }

    public String getFax() {
        return fax;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Account{"
            + "id=" + id
            + ", company='" + company + '\''
            + ", level='" + level + '\''
            + ", website='" + website + '\''
            + ", vanityDomain='" + vanityDomain + '\''
            + ", pluginCampaignId=" + pluginCampaignId
            + ", trackingCodeTemplate='" + trackingCodeTemplate + '\''
            + ", address1='" + address1 + '\''
            + ", address2='" + address2 + '\''
            + ", city='" + city + '\''
            + ", state='" + state + '\''
            + ", territory='" + territory + '\''
            + ", zip=" + zip
            + ", country='" + country + '\''
            + ", phone='" + phone + '\''
            + ", fax='" + fax + '\''
            + ", createdAt=" + createdAt
            + ", updatedAt=" + updatedAt
            + '}';
    }
}
