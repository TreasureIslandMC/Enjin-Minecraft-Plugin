package com.enjin.sponge.config;

import com.enjin.core.config.JsonConfig;
import lombok.Getter;
import lombok.Setter;

public class EnjinConfig extends JsonConfig {
    @Getter @Setter
    private boolean debug = false;
    @Getter @Setter
    private String authkey = "";
    @Getter @Setter
    private boolean https = false;
}