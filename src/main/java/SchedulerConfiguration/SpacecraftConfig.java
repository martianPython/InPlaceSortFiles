package SchedulerConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpacecraftConfig {
    private  long poolingDelay;
    private List< Integer> oneSegmentMessageId;//= {41,21};
    private List< Integer > multiSegmentsPriority1List;//= {17,18};
    private List< Integer > multiSegmentsPriority2List;// = {20};

    private HashMap<String, Boolean> reverseMap;
    private HashMap<String, Boolean> roundRobinMap;
    private HashMap<String, Boolean> firstComeFirstServeMap;
    boolean preemptAfterEachSequence =false;

    public SpacecraftConfig(long poolingDelay, List< Integer > oneSegmentMessageId, List< Integer > multiSegmentsPriority1List, List< Integer > multiSegmentsPriority2List, HashMap<String, Boolean> reverseMap, HashMap<String, Boolean> roundRobinMap, HashMap<String, Boolean> firstComeFirstServeMap, boolean preemptAfterEachSequence) {
        this.poolingDelay = poolingDelay;
        this.oneSegmentMessageId = oneSegmentMessageId;
        this.multiSegmentsPriority1List = multiSegmentsPriority1List;
        this.multiSegmentsPriority2List = multiSegmentsPriority2List;
        this.reverseMap = reverseMap;
        this.roundRobinMap = roundRobinMap;
        this.firstComeFirstServeMap = firstComeFirstServeMap;
        this.preemptAfterEachSequence = preemptAfterEachSequence;
    }

    public SpacecraftConfig() {
    }


    public static  SpacecraftConfig getYamlConfiguration(String satelliteConfig){
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        SpacecraftConfig spacecraftConfig = new SpacecraftConfig();
        try {

             spacecraftConfig = mapper.readValue(new File(satelliteConfig), SpacecraftConfig.class);



        } catch (Exception e) {

            // TODO Auto-generated catch block

            e.printStackTrace();

        }
        return  spacecraftConfig;
    }

    public long getPoolingDelay() {
        return poolingDelay;
    }

    public void setPoolingDelay(long poolingDelay) {
        this.poolingDelay = poolingDelay;
    }

    public List< Integer > getOneSegmentMessageId() {
        return oneSegmentMessageId;
    }

    public void setOneSegmentMessageId(List< Integer > oneSegmentMessageId) {
        this.oneSegmentMessageId = oneSegmentMessageId;
    }

    public List< Integer > getMultiSegmentsPriority1List() {
        return multiSegmentsPriority1List;
    }

    public void setMultiSegmentsPriority1List(List< Integer > multiSegmentsPriority1List) {
        this.multiSegmentsPriority1List = multiSegmentsPriority1List;
    }

    public List< Integer > getMultiSegmentsPriority2List() {
        return multiSegmentsPriority2List;
    }

    public void setMultiSegmentsPriority2List(List< Integer > multiSegmentsPriority2List) {
        this.multiSegmentsPriority2List = multiSegmentsPriority2List;
    }








    public boolean isPreemptAfterEachSequence() {
        return preemptAfterEachSequence;
    }

    public void setPreemptAfterEachSequence(boolean preemptAfterEachSequence) {
        this.preemptAfterEachSequence = preemptAfterEachSequence;
    }

    public HashMap<String, Boolean> getReverseMap() {
        return reverseMap;
    }

    public void setReverseMap(HashMap<String, Boolean> reverseMap) {
        this.reverseMap = reverseMap;
    }

    public HashMap<String, Boolean> getRoundRobinMap() {
        return roundRobinMap;
    }

    public void setRoundRobinMap(HashMap<String, Boolean> roundRobinMap) {
        this.roundRobinMap = roundRobinMap;
    }

    public HashMap<String, Boolean> getFirstComeFirstServeMap() {
        return firstComeFirstServeMap;
    }

    public void setFirstComeFirstServeMap(HashMap<String, Boolean> firstComeFirstServeMap) {
        this.firstComeFirstServeMap = firstComeFirstServeMap;
    }

    @Override
    public String toString() {
        return "SpacecraftConfig{" +
                "poolingDelay=" + poolingDelay +
                ", oneSegmentMessageId=" + oneSegmentMessageId +
                ", multiSegmentsPriority1List=" + multiSegmentsPriority1List +
                ", multiSegmentsPriority2List=" + multiSegmentsPriority2List +
                ", reverseMap=" + reverseMap +
                ", roundRobinMap=" + roundRobinMap +
                ", firstComeFirstServeMap=" + firstComeFirstServeMap +
                ", preemptAfterEachSequence=" + preemptAfterEachSequence +
                '}';
    }
}
