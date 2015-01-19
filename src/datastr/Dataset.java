package datastr;
import java.util.*;

public class Dataset {
	
	public ArrayList<Video> videos;
	public HashMap<String, Integer> tagsMap;
	public HashMap<Integer, String> invTagsMap;
	public HashMap<Integer, String> invTagsMapM;
	public HashMap<Integer, String> invTagsMapB;
	public HashMap<String, Integer> tagsCountsMap;
	
	public HashMap<String, Integer> tagsMapB;
	public HashMap<String, Integer> tagsMapM;
	public HashMap<String, String> videoIDs;
	
	public HashMap<String, Integer> tagToClusterMap;
	public int tagNetwork[][];
	public int tagNetworkM[][];
	public int tagNetworkB[][];
	public int tagID = 0;
	public int nEdges = 0;
	public int nEdgesM = 0;
	public int nEdgesB = 0;
	public int clusterID = 1;
	
	public Dataset()
	{
		this.videos = new ArrayList<Video>();
		this.tagsMap = new HashMap<String, Integer>();
		this.tagsMapB = new HashMap<String, Integer>();
		this.tagsMapM = new HashMap<String, Integer>();
		this.invTagsMap = new HashMap<Integer, String>();
		this.invTagsMapM = new HashMap<Integer, String>();
		this.invTagsMapB = new HashMap<Integer, String>();
		this.tagsCountsMap = new HashMap<String, Integer>();
		this.videoIDs = new HashMap<String, String>();
		this.tagToClusterMap = new HashMap<String, Integer>();
	}
	
	public void addVideo(Video v)
	{
		this.videos.add(v);
	}
}
