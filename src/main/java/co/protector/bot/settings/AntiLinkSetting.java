package co.protector.bot.settings;

import co.protector.bot.core.settings.GuildSetting;
import co.protector.bot.core.settings.types.BooleanSettingType;

public class AntiLinkSetting extends GuildSetting<BooleanSettingType> {
    @Override
    protected BooleanSettingType getSettingsType() {
        return new BooleanSettingType();
    }

    @Override
    public String getKey() {
        return "antilink";
    }

    @Override
    public String dbTable() {
        return "antilink";
    }

    @Override
    public String dbField() {
        return "bool";
    }

    @Override
    public String getDefault() {
        return "false";
    }

    @Override
    public String getDescription() {
        return "Use the anti link feature? \n\n" +
                "true -> enabled\n" +
                "false -> disabled";
    }
}