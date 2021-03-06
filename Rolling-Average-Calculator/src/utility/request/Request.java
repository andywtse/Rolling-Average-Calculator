package utility.request;

import com.sun.org.apache.regexp.internal.RE;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Request implements Serializable{
    
    private static final String ID_KEY = "ID";
    private static final String TOPIC_KEY = "TOPIC";
    private static final String DATA_KEY = "DATA";
    private static final String AMOUNT_KEY = "AMOUNT";
    private static final String RANGE_KEY = "RANGE";
    private static final String ENTRIES_KEY = "ENTRIES";
    private static final String RESPONSE_KEY = "RESPONSE";
    private List<Integer> entries;
    private Topic topic;
    private Range range;
    private Response response;
    private long id;
    private int amount;
    private boolean hasAmount;
    private boolean hasId;
    
    private Request( final List<Integer> entries, final Topic topic, final Range range, final Response response, final boolean hasId, final long id, final boolean hasAmount, final int amount ) {
        
        this.entries = entries;
        this.topic = topic;
        this.range = range;
        this.response = response;
        this.hasId = hasId;
        this.id = id;
        this.hasAmount = hasAmount;
        this.amount = amount;
    }
    
    public List<Integer> getEntries() {
        
        return entries;
    }
    
    public Topic getTopic() {
        
        return topic;
    }
    
    public Range getRange() {
        
        return range;
    }
    
    public Response getResponse() {
        
        return response;
    }
    
    public long getId() {
        
        return id;
    }
    
    public int getAmount() {
        
        return amount;
    }
    
    public String toJSONString() {
        
        try {
            final JSONObject intermediate = new JSONObject();
            intermediate.put(TOPIC_KEY, topic);
            if (hasId) {
                intermediate.put(ID_KEY, id);
            }
            if (response != null) {
                intermediate.put(RESPONSE_KEY, response);
            }
            
            final JSONObject data = new JSONObject();
            if (entries != null) {
                data.put(ENTRIES_KEY, entries);
            }
            if (range != null) {
                data.put(RANGE_KEY, range);
            }
            if (hasAmount) {
                data.put(AMOUNT_KEY, amount);
            }
            intermediate.put(DATA_KEY, data);
            return intermediate.toString();
        } catch (final JSONException exception) {
            return null;
        }
    }
    
    public enum Topic {
        SUBMIT, AVERAGE, COUNT, HISTORY, USERS, DISCONNECT,
    }
    
    public enum Range {
        ALL, SELF,
    }
    
    public enum Response {
        OK, ERROR,
    }
    
    public static class Builder {
        
        private List<Integer> entries = null;
        private Topic topic = null;
        private Range range = null;
        private Response response = null;
        private long id = -1;
        private int amount;
        private boolean hasAmount = false;
        private boolean hasId = false;
        
        public Builder entries( final List<Integer> entries ) {
            
            this.entries = entries;
            return this;
        }
        
        public Builder topic( final Topic topic ) {
            
            this.topic = topic;
            return this;
        }
    
        public Builder range( final Range range ) {
            
            this.range = range;
            return this;
        }
    
        public Builder response( final Response response ) {
            
            this.response = response;
            return this;
        }
    
        public Builder id( final long id ) {
            
            this.id = id;
            this.hasId = true;
            return this;
        }
    
        public Builder amount( final int amount ) {
            
            this.amount = amount;
            this.hasAmount = true;
            return this;
        }
    
        public Builder fromJSONString( final String inputString ) {
            
            final JSONObject input = new JSONObject(inputString);
            try {
                if (input.has(TOPIC_KEY)) {
                    topic(Topic.valueOf(input.getString(TOPIC_KEY)));
                } else {
                    return this;
                }
                if (input.has(RESPONSE_KEY)) {
                    response(Response.valueOf(input.getString(RESPONSE_KEY)));
                }
                if (input.has(ID_KEY)) {
                    id(input.getLong(ID_KEY));
                }
                if (!input.has(DATA_KEY)) {
                    topic = null;
                    return this;
                }
                final JSONObject data = input.getJSONObject(DATA_KEY);
                if (data.has(RANGE_KEY)) {
                    range(Range.valueOf(data.getString(RANGE_KEY)));
                }
                if (data.has(AMOUNT_KEY)) {
                    amount(data.getInt(AMOUNT_KEY));
                }
                if (data.has(ENTRIES_KEY)) {
                    final JSONArray dataEntries = data.getJSONArray(ENTRIES_KEY);
                    final List<Integer> inputEntries = new ArrayList<>(dataEntries.length());
                    for (int i = 0; i < dataEntries.length(); ++i) {
                        inputEntries.add(dataEntries.getInt(i));
                    }
                    entries(inputEntries);
                }
            } catch (final JSONException exception) {
                // Nothing to do.
            }
            return this;
        }
    
        public Request build() {
            
            if (topic == null) {
                return null;
            }
            return new Request(entries, topic, range, response, hasId, id, hasAmount, amount);
        }
    }
}
