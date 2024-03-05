package com.micewine.emu.models;

public class SettingsList {
   private int SettingsTitle, DescriptionSettings;
    private int ImageSettings;

    public SettingsList(int SettingsTitle, int DescriptionSettings, int ImageSettings) {
        this.SettingsTitle = SettingsTitle;
        this.DescriptionSettings = DescriptionSettings;
        this.ImageSettings = ImageSettings;
    }

    public int getTitleSettings() {
        return SettingsTitle;
    }

    public void setTitleSettings(int SettingsTitle) {
        this.SettingsTitle = SettingsTitle;
    }

    public int getDescriptionSettings() {
        return DescriptionSettings;
    }

    public void setDescriptionSettings(int DescriptionSettings) {
        this.DescriptionSettings = DescriptionSettings;
    }

    public int getImageSettings() {
        return ImageSettings;
    }

    public void setImageSettings(int ImageSettings) {
        this.ImageSettings = ImageSettings;
    }
}
