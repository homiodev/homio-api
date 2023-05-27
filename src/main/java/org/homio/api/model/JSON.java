package org.homio.api.model;

import lombok.NoArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;

@NoArgsConstructor
public class JSON extends JSONObject {

    public JSON(String source) throws JSONException {
        super(source);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JSONObject)) return false;
        return this.toString().equals(o.toString());
    }
}
