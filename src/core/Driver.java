package core;
import datastr.*;
import externalUtil.CC;
import externalUtil.Graph;
import externalUtil.In;
import externalUtil.StdOut;

import java.io.*;
import java.text.ParseException;
import java.util.*;
public class Driver {
	
	public static Dataset d;
	
	public static void main(String[] args) throws IOException, ParseException{
		d = new Dataset();
		new ReadCVSData(d);
		System.out.println("total no of vidIDs populated: "+d.videoIDs.size());
		seperateTags(15000);
		//populateTagNetwork();
		populateTagNetworkM();
		populateTagNetworkB();
		
		//thresholdTagNetwork(2500);
		thresholdTagNetworkM(400);
		thresholdTagNetworkB(5000);
		
		//populateGraphFileForConnectedComponents();
		populateGraphFileForConnectedComponentsM();
		populateGraphFileForConnectedComponentsB();
		
		//findConnectedComponents();
		findConnectedComponentsM();
		findConnectedComponentsB();
		System.out.println("final clusterID: "+d.clusterID+" --- "+d.tagToClusterMap.size());
		populateClusterFiles();
	}
	
	public static void populateClusterFiles() throws IOException
	{
		int numClusters = d.clusterID-1;
		int counts[] = new int[numClusters];
		HashMap<Integer, FileWriter> fileOuts = new HashMap<Integer, FileWriter>();
		/*for(int i=1;i<=numClusters;i++)
		{
			FileWriter fstream = new FileWriter("cluster/cluster"+i+".txt", true);
			//BufferedWriter out = new BufferedWriter(fstream);
			fileOuts.put(new Integer(i), fstream);
		}*/
		// now iterate through the entire vid dataset and populate cluster files accordingly
		// for any vid, assign this vid to the tag/cluster which has the min no vids
		Iterator<Video> itr = d.videos.iterator();
		while(itr.hasNext())
		{
			Video v = itr.next();
			Iterator<String> itr1 = v.tags.iterator();
			int minVid = 1000000; int finalCluster=1;
			while(itr1.hasNext())
			{
				String tag = itr1.next();
				if(tag.length()<1) continue;
				int clusterNo = d.tagToClusterMap.get(tag);
				//if(counts[clusterNo] < minVid)
				if(d.tagsCountsMap.get(tag) < minVid)
				{
					finalCluster = clusterNo;
					//minVid = counts[clusterNo];
					minVid = d.tagsCountsMap.get(tag);
				}
			}
			// now assign this vid to the finalCluster tag/cluster
			//BufferedWriter out = fileOuts.get(finalCluster);
			
			FileWriter fstream = new FileWriter("cluster/cluster"+finalCluster+".txt", true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(v.videoID+"");
			out.write("\n");
			out.close();
		}
		// close all outs now
		/*Iterator<BufferedWriter> itt = fileOuts.values().iterator();
		while(itt.hasNext())
		{
			BufferedWriter out = itt.next();
			out.close();
		}*/
	}
	
	public static void seperateTags(int thres)
	{
		int tagIDM=0, tagIDB=0;
		Iterator<String> itr = d.tagsMap.keySet().iterator();
		while(itr.hasNext())
		{
			String tag = itr.next();
			int freq = d.tagsCountsMap.get(tag);
			if(freq>thres)
			{
				d.tagsMapB.put(tag, new Integer(tagIDB));
				d.invTagsMapB.put(new Integer(tagIDB), tag);
				tagIDB++;
			}
			else
			{
				d.tagsMapM.put(tag, new Integer(tagIDM));
				d.invTagsMapM.put(new Integer(tagIDM), tag);
				tagIDM++;
			}
		}
		System.out.println("BIG Map size: "+d.tagsMapB.size()+"  --  Medium map size: "+d.tagsMapM.size());
	}
	
	public static void thresholdTagNetwork(int thres)
	{
		int networkSize = d.tagsMap.size();
		for(int i=0;i<networkSize;i++)
		{
			for(int j=0;j<networkSize;j++)
			{
				if(d.tagNetwork[i][j]>thres) {d.tagNetwork[i][j] = 1;d.nEdges++;}
				else d.tagNetwork[i][j] = 0;
			}
		}
		d.nEdges = d.nEdges/2;
	}
	
	public static void thresholdTagNetworkM(int thres)
	{
		int networkSize = d.tagsMapM.size();
		for(int i=0;i<networkSize;i++)
		{
			for(int j=0;j<networkSize;j++)
			{
				if(d.tagNetworkM[i][j]>thres) {d.tagNetworkM[i][j] = 1;d.nEdgesM++;}
				else d.tagNetworkM[i][j] = 0;
			}
		}
		d.nEdgesM = d.nEdgesM/2;
	}
	
	public static void thresholdTagNetworkB(int thres)
	{
		int networkSize = d.tagsMapB.size();
		for(int i=0;i<networkSize;i++)
		{
			for(int j=0;j<networkSize;j++)
			{
				if(d.tagNetworkB[i][j]>thres) {d.tagNetworkB[i][j] = 1;d.nEdgesB++;}
				else d.tagNetworkB[i][j] = 0;
			}
		}
		d.nEdgesB = d.nEdgesB/2;
	}
	
	public static void findConnectedComponents()
	{
		In in = new In("tagGraph");
		Graph G = new Graph(in);
		CC cc = new CC(G);

		// number of connected components
		int M = cc.count();
		System.out.println("No of connected components: "+M);
		// compute list of vertices in each connected component
		externalUtil.Queue<Integer>[] components = (externalUtil.Queue<Integer>[]) new externalUtil.Queue[M];
		for (int i = 0; i < M; i++) {
			components[i] = new externalUtil.Queue<Integer>();
		}
		for (int v = 0; v < G.V(); v++) {
			components[cc.id(v)].enqueue(v);
		}

		// print results
		int temp=0;
		for (int i = 0; i < M; i++) {
			//System.out.println("--------");
			StdOut.print("\nSize:"+components[i].size()+"------");
			for (int v : components[i]) {
			  StdOut.print(d.invTagsMap.get(v) + "__");
			}
			
			if(components[i].size() > 1) temp++;
		}
		System.out.println("----"+temp);
		System.out.println(M+"--"+components.length);
	}
	
	public static void findConnectedComponentsM()
	{
		In in = new In("tagGraphM");
		Graph G = new Graph(in);
		CC cc = new CC(G);

		// number of connected components
		int M = cc.count();
		System.out.println("No of connected components: "+M);
		// compute list of vertices in each connected component
		externalUtil.Queue<Integer>[] components = (externalUtil.Queue<Integer>[]) new externalUtil.Queue[M];
		for (int i = 0; i < M; i++) {
			components[i] = new externalUtil.Queue<Integer>();
		}
		for (int v = 0; v < G.V(); v++) {
			components[cc.id(v)].enqueue(v);
		}

		// print results
		int temp=0;
		for (int i = 0; i < M; i++) {
			//System.out.println("--------");
			StdOut.print("\nSize:"+components[i].size()+"------");
			for (int v : components[i]) {
			  StdOut.print(d.invTagsMapM.get(v) + "__");
			  String tag = d.invTagsMapM.get(v);
			  d.tagToClusterMap.put(tag, new Integer(d.clusterID));
			}
			d.clusterID++;
			if(components[i].size() > 1) temp++;
		}
		System.out.println("----"+temp);
		System.out.println(M+"--"+components.length);
	}
	
	public static void findConnectedComponentsB()
	{
		In in = new In("tagGraphB");
		Graph G = new Graph(in);
		CC cc = new CC(G);

		// number of connected components
		int M = cc.count();
		System.out.println("No of connected components: "+M);
		// compute list of vertices in each connected component
		externalUtil.Queue<Integer>[] components = (externalUtil.Queue<Integer>[]) new externalUtil.Queue[M];
		for (int i = 0; i < M; i++) {
			components[i] = new externalUtil.Queue<Integer>();
		}
		for (int v = 0; v < G.V(); v++) {
			components[cc.id(v)].enqueue(v);
		}

		// print results
		int temp=0;
		for (int i = 0; i < M; i++) {
			//System.out.println("--------");
			StdOut.print("\nSize:"+components[i].size()+"------");
			for (int v : components[i]) {
			  StdOut.print(d.invTagsMapB.get(v) + "__");
			  String tag = d.invTagsMapB.get(v);
			  d.tagToClusterMap.put(tag, new Integer(d.clusterID));
			}
			d.clusterID++;
			if(components[i].size() > 1) temp++;
		}
		System.out.println("----"+temp);
		System.out.println(M+"--"+components.length);
	}
	
	public static void populateGraphFileForConnectedComponents() throws IOException
	{
		int networkSize = d.tagsMap.size();
		FileWriter fstream = new FileWriter("tagGraph");
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(networkSize+"\n");
		out.write(d.nEdges+"\n");
		for(int i=0;i<networkSize;i++)
		{
			for(int j=i+1;j<networkSize;j++)
			{
				if(d.tagNetwork[i][j]>0) out.write(i+" "+j+"\n");
			}
		}
		out.close();
	}
	
	public static void populateGraphFileForConnectedComponentsM() throws IOException
	{
		int networkSize = d.tagsMapM.size();
		FileWriter fstream = new FileWriter("tagGraphM");
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(networkSize+"\n");
		out.write(d.nEdgesM+"\n");
		for(int i=0;i<networkSize;i++)
		{
			for(int j=i+1;j<networkSize;j++)
			{
				if(d.tagNetworkM[i][j]>0) out.write(i+" "+j+"\n");
			}
		}
		out.close();
	}
	
	public static void populateGraphFileForConnectedComponentsB() throws IOException
	{
		int networkSize = d.tagsMapB.size();
		FileWriter fstream = new FileWriter("tagGraphB");
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(networkSize+"\n");
		out.write(d.nEdgesB+"\n");
		for(int i=0;i<networkSize;i++)
		{
			for(int j=i+1;j<networkSize;j++)
			{
				if(d.tagNetworkB[i][j]>0) out.write(i+" "+j+"\n");
			}
		}
		out.close();
	}
	
	public static void populateTagNetwork()
	{
		int networkSize = d.tagsMap.size();
		d.tagNetwork = new int[networkSize][networkSize];
		/*Iterator<String> itr = d.tagsMap.keySet().iterator();
		while(itr.hasNext())
		{
			String tag = itr.next();
			int id = d.tagsMap.get(tag);
			System.out.println(tag+"-"+id);
		}*/
		Iterator<Video> itr = d.videos.iterator();
		while(itr.hasNext())
		{
			Video v = itr.next();
			int s = v.tags.size();
			for(int i=0;i<s;i++)
			{
				for(int j=i+1;j<s;j++)
				{
					String tag1 = v.tags.get(i);
					String tag2 = v.tags.get(j);
					int id1 = d.tagsMap.get(tag1);
					int id2 = d.tagsMap.get(tag2);
					d.tagNetwork[id1][id2]++;
					d.tagNetwork[id2][id1]++;
				}
			}
		}
		/*for(int i=0;i<networkSize;i++)
		{
			for(int j=0;j<networkSize;j++)
			{
				System.out.print(d.tagNetwork[i][j]+"_");
			}
			System.out.println();
		}*/
	}
	public static void populateTagNetworkM()
	{
		int networkSize = d.tagsMapM.size();
		d.tagNetworkM = new int[networkSize][networkSize];
		Iterator<Video> itr = d.videos.iterator();
		while(itr.hasNext())
		{
			Video v = itr.next();
			int s = v.tags.size();
			for(int i=0;i<s;i++)
			{
				for(int j=i+1;j<s;j++)
				{
					String tag1 = v.tags.get(i);
					String tag2 = v.tags.get(j);
					// if either of the tags isnt there, just skip it
					if(!d.tagsMapM.containsKey(tag1)) continue;
					if(!d.tagsMapM.containsKey(tag2)) continue;
					int id1 = d.tagsMapM.get(tag1);
					int id2 = d.tagsMapM.get(tag2);
					d.tagNetworkM[id1][id2]++;
					d.tagNetworkM[id2][id1]++;
				}
			}
		}
	}
	public static void populateTagNetworkB()
	{
		int networkSize = d.tagsMapB.size();
		d.tagNetworkB = new int[networkSize][networkSize];
		Iterator<Video> itr = d.videos.iterator();
		while(itr.hasNext())
		{
			Video v = itr.next();
			int s = v.tags.size();
			for(int i=0;i<s;i++)
			{
				for(int j=i+1;j<s;j++)
				{
					String tag1 = v.tags.get(i);
					String tag2 = v.tags.get(j);
					if(!d.tagsMapB.containsKey(tag1)) continue;
					if(!d.tagsMapB.containsKey(tag2)) continue;
					int id1 = d.tagsMapB.get(tag1);
					int id2 = d.tagsMapB.get(tag2);
					d.tagNetworkB[id1][id2]++;
					d.tagNetworkB[id2][id1]++;
				}
			}
		}
	}
}