package core;
import datastr.*;
import externalUtil.CC;
import externalUtil.Graph;
import externalUtil.In;
import externalUtil.StdOut;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
public class Driver {
	
	public static Dataset d;
	
	public static void main(String[] args) throws IOException, ParseException{
		d = new Dataset();
		new ReadCVSData(d);
		System.out.println("total no of vidIDs populated: "+d.videoIDs.size()+"_"+d.videoIDToVideoMap.size());
		int stage = 2;
		if(stage == 1)
		{
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
		else if(stage == 2)
		{
			// now we have all the videos in the Dataset d, we need to do that factor disentangling
			readClusterFiles();
		}
	}
	
	public static void readClusterFiles() throws IOException, ParseException
	{
		int clusterNo = 1;
		
		BufferedReader br1 = new BufferedReader(new FileReader("bounds_split90.txt"));
		
		BufferedReader br2 = new BufferedReader(new FileReader("split90_result.txt"));
		String lineparam = br2.readLine();
		String linedate = br1.readLine();
		//String lineparam = "";
		//String linedate = "";
		while(clusterNo<=37 && lineparam!=null)
		{
			
			System.out.println("CLUSTER "+clusterNo+"-----------------------------");
			//System.out.println(linedate);
			String parts[] = linedate.split("\t");
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date start = df.parse(parts[0].substring(1, parts[0].length()-1));
			Date end = df.parse(parts[1].substring(1, parts[1].length()-1));
			
			String parts1[] = lineparam.split("\t");
			double mu = Double.parseDouble(parts1[0]);
			double w = Double.parseDouble(parts1[1]);
			double beta = Double.parseDouble(parts1[2]);
			lineparam = br2.readLine();
			linedate = br1.readLine();
			//System.out.println(mu+"__"+w+"__"+beta);
			BufferedReader br = new BufferedReader(new FileReader("cluster/cluster"+clusterNo+".txt"));
			String line = br.readLine();
			
			int cc=0;
			//if(cc==0) continue;
			HashMap<Integer, ArrayList<Video>> dayToVideoList = new HashMap<Integer, ArrayList<Video>>();
			ArrayList<Integer> dayList = new ArrayList<Integer>();
			HashMap<String, ArrayList<Video>> userVidListMap = new HashMap<String, ArrayList<Video>>();
			while(line!=null)
			{
				cc++;
				int videoID = Integer.parseInt(line);
				Video v = d.videoIDToVideoMap.get(videoID);
				int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
				//start = start.getTime()-MILLIS_IN_DAY;
				//TimeUnit.
				if(v.uploadDate.compareTo(start)>=0 && v.uploadDate.compareTo(end)<=0);
				//if(v.uploadDate.after(start) && v.uploadDate.before(end));
				else {line = br.readLine();continue;}
				// now populate this video to this cluster's daytoVid hashmap
				// get Day No
				//int dayNo = getDayNo(v);
				//Date initialDate = new Date(2013-1900, 8, 31);
				//System.out.println("Initial Date: "+initialDate.toGMTString());
				
				//user Map START
				if(userVidListMap.containsKey(v.user))
				{
					ArrayList<Video> vList = userVidListMap.get(v.user);
					vList.add(v);
					userVidListMap.put(v.user, vList);
				}
				else
				{
					ArrayList<Video> vList = new ArrayList<Video>();
					vList.add(v);
					userVidListMap.put(v.user, vList);
				}
				//user Map END
				
				int dayNo = (int) getDateDiff(start, v.uploadDate, TimeUnit.DAYS);
				if(dayToVideoList.containsKey(dayNo))
				{
					ArrayList<Video> vList = dayToVideoList.get(dayNo);
					vList.add(v);
					dayToVideoList.put(dayNo,  vList);
				}
				else
				{
					ArrayList<Video> vList = new ArrayList<Video>();
					vList.add(v);
					dayToVideoList.put(dayNo, vList);
					dayList.add(dayNo);
				}
				line = br.readLine();
			}
			//System.out.println("For this cluster, hashmap size: "+dayToVideoList.size()+" dayList size: "+dayList.size());
			//System.out.println(cc);
			// now we have populated the DayToVideoList HashMap needed for rest of the calculation for this cluster
			// now we go to the main calculations
			//int ti, tj;
			Collections.sort(dayList);
			//for(int i =0;i<dayList.size();i++) System.out.println(dayToVideoList.get(dayList.get(i)).size());
			double selfD=0, selfN=0, socialN = 0, num=0, den=0;
			//System.out.println("UserVideoMap: "+userVidListMap.size());
			// work om finding user's avg
			HashMap<String, Double> userAvg = new HashMap<String, Double>();
			Iterator<String> itr = userVidListMap.keySet().iterator();
			while(itr.hasNext())
			{
				String user = itr.next();
				ArrayList<Video> vList = userVidListMap.get(user);
				int size = vList.size();
				Iterator<Video> itrv = vList.iterator();
				int numViewsComments = 0;
				while(itrv.hasNext())
				{
					Video v = itrv.next();
					numViewsComments+= v.nViews;
					numViewsComments+= v.nComments;
				}
				double avgVC = (double)numViewsComments/(double)size;
				//System.out.println("Populating avg VC for user: "+avgVC+" size: "+size+" sumVC: "+numViewsComments);
				userAvg.put(user, avgVC);
			}
			//System.exit(0);
			for(int j=0;j<dayList.size();j++)
			{
				int tj = dayList.get(j);
				den = 0;
				double carry=0;
				ArrayList<Video> vjList = dayToVideoList.get(tj);
				int ttj = 0;
				while(ttj<vjList.size())
				{
					Video vj = vjList.get(ttj);
					for(int i=0;i<j;i++)
					{
						int ti = dayList.get(i);
						ArrayList<Video> viList = dayToVideoList.get(ti);
						int nV = viList.size();
						int tti=0;
						while(tti<nV)
						{
							Video vi = viList.get(tti);
							double g = (beta*Math.exp(-1*w*(tj-ti)));
							carry += g;
							num = g;
							den = mu+carry;
							selfD += (num/den);
							if(vj.user.compareTo(vi.user)==0) selfN+= (num/den);
							// SOCIAL START
							double avgOfvjUser = userAvg.get(vj.user);
							if((vi.nComments+vi.nViews) > avgOfvjUser) socialN+= (num/den);
							// SOCIAL END
							tti++;
						}
						//System.out.println(num+" / "+den+"__"+carry+"____"+selfD);
					}
					ttj++;
				}
				
				//System.out.println("--------------------------------------"+selfD);
				//if(j==3) System.exit(0);
			}
			System.out.println("Self-Reinforcing Effect "+selfN/selfD+"\t Popularity Effect"+ socialN/selfD);
			clusterNo++;
			//break;
		}
	}
	
	public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
	    long diffInMillies = date2.getTime() - date1.getTime();
	    return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
	}
	
	public static int getDayNo(Video v)
	{
		int dayNo = 0;
		return dayNo;
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