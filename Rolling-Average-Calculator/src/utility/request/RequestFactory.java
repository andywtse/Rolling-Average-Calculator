package utility.request;

import java.util.List;

public class RequestFactory {
    
    public static Request clientSubmitRequest( final long id, final int amount ) {
        
        return new Request.Builder().id(id).topic(Request.Topic.SUBMIT).amount(amount).build();
    }
    
    public static Request clientAverageRequest( final long id, final Request.Range range ) {
        
        return new Request.Builder().id(id).topic(Request.Topic.AVERAGE).range(range).build();
    }
    
    public static Request clientCountRequest( final long id, final Request.Range range ) {
        
        return new Request.Builder().id(id).topic(Request.Topic.COUNT).range(range).build();
    }
    
    public static Request clientHistoryRequest( final long id, final Request.Range range ) {
        
        return new Request.Builder().id(id).topic(Request.Topic.HISTORY).range(range).build();
    }
    
    public static Request clientUsersRequest( final long id ) {
        
        return new Request.Builder().id(id).topic(Request.Topic.USERS).build();
    }
    
    public static Request clientDisconnect( final long id ) {
        
        return new Request.Builder().id(id).topic(Request.Topic.DISCONNECT).build();
    }
    
    public static Request serverSubmitResponse( final Request.Response response, final int amount ) {
        
        return new Request.Builder().topic(Request.Topic.SUBMIT).response(response).amount(amount).build();
    }
    
    public static Request serverAverageResponse( final Request.Response response, final Request.Range range, final int amount ) {
        
        return new Request.Builder().topic(Request.Topic.AVERAGE).response(response).range(range).amount(amount).build();
    }
    
    public static Request serverCountResponse( final Request.Response response, final Request.Range range, final int amount ) {
        
        return new Request.Builder().topic(Request.Topic.COUNT).response(response).range(range).amount(amount).build();
    }
    
    public static Request serverHistoryResponse( final Request.Response response, final Request.Range range, final List<Integer> entries ) {
        
        return new Request.Builder().topic(Request.Topic.HISTORY).response(response).range(range).entries(entries).build();
    }
    
    public static Request serverUsersRequest( final Request.Response response, final int amount ) {
        
        return new Request.Builder().topic(Request.Topic.USERS).response(response).amount(amount).build();
    }
    
    public static Request serverDisconnect() {
        
        return new Request.Builder().topic(Request.Topic.DISCONNECT).build();
    }
    
    public static Request fromJSONString( final String inputString ) {
        
        return new Request.Builder().fromJSONString(inputString).build();
    }
}
