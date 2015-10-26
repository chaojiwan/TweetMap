package eb;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.Date;

/**
 * Created by slgu1 on 10/8/15.
 */
public class ElasticBean {

    private static final String appPath = "/Users/slgu1/aws/tweetmap/target/tweetmap.war";
    public void init() throws IOException{
        credentials = new PropertiesCredentials(
                ElasticBean.class.getResourceAsStream("rootkey.csv"));
        PropertyConfigurator.configure("log4j.properties");
        eb = new AWSElasticBeanstalkClient(credentials);
        s3 = new AmazonS3Client();
    }
    public void setinfo(String appName, String appDesc, String envName, String envDesc) {
        this.appName = appName;
        this.appDesc = appDesc;
        this.envName = envName;
        this.envDesc = envDesc;
    }

    public void setup() throws IOException {
        //create application
        eb.createApplication(new CreateApplicationRequest().
                withApplicationName(appName).withDescription(appDesc));

        //create environment
        eb.createEnvironment(new CreateEnvironmentRequest().withEnvironmentName(envName)
                .withApplicationName(appName)
                .withSolutionStackName("32bit Amazon Linux running Tomcat 7")
                .withDescription(envDesc));
    }
    public void deploy(String appVersionDesc) throws IOException, InterruptedException{
        //check health
        while (true) {
            //every 4 seconds check or will get exception
            Thread.sleep(1000);
            DescribeEnvironmentsResult res = eb.describeEnvironments(new DescribeEnvironmentsRequest()
                    .withApplicationName(appName));
            boolean flg = false;
            for (EnvironmentDescription desc: res.getEnvironments()) {
                System.out.println(desc.getEnvironmentName() + "," + desc.getStatus());
                if (desc.getStatus().equals("Ready")) {
                    //OK
                    flg = true;
                    break;
                }
            }
            if (flg)
                break;
        }

        //create S3 storage to upload the web files
        File f = new File(appPath);
        String fileName = f.getName();
        String bucketName = eb.createStorageLocation().getS3Bucket();
        s3.putObject(bucketName, fileName, f);

        //upload application
        String version_number = fileName + "_" + new Date().toString();
        eb.createApplicationVersion(new CreateApplicationVersionRequest().withApplicationName(appName)
                .withDescription(appVersionDesc).withVersionLabel(version_number)
                .withAutoCreateApplication(true)
                .withSourceBundle(new S3Location(bucketName, fileName)));

        //update version
        eb.updateEnvironment(new UpdateEnvironmentRequest()
                .withEnvironmentName(envName)
                .withVersionLabel(version_number));
    }

    private AWSCredentials credentials;
    private AWSElasticBeanstalkClient eb;
    private AmazonS3 s3;
    private String appName;
    private String appDesc;
    private String envName;
    private String envDesc;
    public static void main(String[] args) {
        ElasticBean proxyer = new ElasticBean();
        try {
            System.out.println("init sdk");
            proxyer.init();
            System.out.println("set info");
            proxyer.setinfo("tweetmap", "slgu@tweetmap", "tomcat7", "slgu@tomcat7");
            System.out.println("begin setup");
            proxyer.setup();
            //wait for ready
            System.out.println("begin deploy");
            proxyer.deploy("update test application");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}