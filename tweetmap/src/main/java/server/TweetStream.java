package server;

import db.Tweet;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
/**
 * Created by slgu1 on 10/10/15.
 */
public class TweetStream {
    static public BlockingDeque <Tweet> queue = new LinkedBlockingDeque<Tweet>();
    static boolean flg = false;
    static int MAXN_LENGTH = 1000;
    public static void initService() throws IOException{
        System.out.println("init service");
        if (flg)
            return;
        if (!flg) {
            flg = true;
        }
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("")
                .setOAuthConsumerSecret("")
                .setOAuthAccessToken("")
                .setOAuthAccessTokenSecret("");
        /**/
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        StatusListener listener = new StatusListener() {
            public void onStatus(Status status){
                if (queue.size() >= MAXN_LENGTH)
                    return;
                String splitString = "@x@x@x";
                String username = status.getUser().getScreenName();
                double lontitude = -1, latitude = -1;
                //set -1 to no geolocation
                if (status.getGeoLocation() == null) {
                    //substitude
                    Place place = status.getPlace();
                    if (place == null || place.getBoundingBoxCoordinates() == null)
                        return;
                    GeoLocation loc = place.getBoundingBoxCoordinates()[0][0];
                    latitude = loc.getLatitude();
                    lontitude = loc.getLongitude();
                }
                else {
                    lontitude = status.getGeoLocation().getLongitude();
                    latitude = status.getGeoLocation().getLatitude();
                }
                long id = status.getId();
                Date date = status.getCreatedAt();
                String context = status.getText();
                context.replaceAll("\n"," ");
                //create tweet
                Tweet newItem = new Tweet();
                newItem.setId(String.valueOf(id));
                newItem.setUserName(username);
                newItem.setLatitude(latitude);
                newItem.setLontitude(lontitude);
                newItem.setText(context);
                newItem.setCreateTime(date);
                //add to queue
                queue.add(newItem);
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                //System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                //System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                //System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                //System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        twitterStream.addListener(listener);
        //filter
        FilterQuery query = new FilterQuery();
        //just english
        query.language(new String[]{"en"});
        //Some track
        query.track(new String[]{"food","sport","music","tech"});
        //try new york
        //query.locations(new double[][]{{-74.021177, 40.703283}, {-73.928416, 40.811226}});
        //just America
        query.locations(new double[][]{{-124.384632, 27.936095}, {-73.847522, 49.396611}});
        twitterStream.filter(query);
    }
    public static void main(String [] args) throws IOException{
        TweetStream.initService();
        while (true) {

        }
    }
}