package com.search.project.yelp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.search.project.yelp.datatypes.Business;
import com.search.project.yelp.datatypes.Review;
import com.search.project.yelp.datatypes.Tip;

public class IndexData {

	private String indexPath;
	private String corpusPath;
	private String sourcePath;

	public IndexData() {
		URL location = Review.class.getProtectionDomain().getCodeSource()
				.getLocation();
		String srcPath = location.toString().replace("file:/", "")
				.replace("bin", "src");
		setSourcePath(srcPath);
	}

	public String getIndexPath() {
		return indexPath;
	}

	public void setIndexPath(String indexPath) {
		this.indexPath = indexPath;
	}

	public String getCorpusPath() {
		return corpusPath;
	}

	public void setCorpusPath(String corpusPath) {
		this.corpusPath = corpusPath;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public void generateIndex(String object) {
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(".\\IndexingProgress.txt");
			System.out.println("Indexing " + object + " to directory '"
					+ getIndexPath() + "/" + object + "'...");
			fileWriter.write("Indexing to directory '" + getIndexPath() + "/"
					+ object + "'...\n");

			Directory dir = FSDirectory.open(Paths.get(indexPath + "\\"
					+ object));
			Analyzer analyzer = new StandardAnalyzer();
			//try using whitespaceanalyzer here to avoid - being replaced by whitespace
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

			iwc.setOpenMode(OpenMode.CREATE);

			IndexWriter writer;
			writer = new IndexWriter(dir, iwc);

			// fetch all the files from the corpus path
			File corpus = new File(getSourcePath() + getCorpusPath());
			File[] files = corpus.listFiles();

			for (File file : files) {
				// condition to neglect files other than files we want
				if (!file.getName().contains(object))
					continue;

				BufferedReader br = new BufferedReader(new FileReader(file));
				String lineInFile = "";
				Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd")
						.create();

				while ((lineInFile = br.readLine()) != null) {
					Document ldoc = new Document();
					switch (object) {
					case "review":
						Review review = null;
						review = new Review();
						prepareAndIndexReview(gson, review, ldoc, lineInFile);
						break;
					case "tip":
						Tip tip = null;
						tip = new Tip();
						prepareAndIndexTip(gson, tip, ldoc, lineInFile);
						break;
					case "business":
						Business business = null;
						business = new Business();
						prepareAndIndexBusiness(gson, business, ldoc,
								lineInFile);
						break;
					}
					writer.addDocument(ldoc);
				}

				writer.forceMerge(1);
				writer.commit();
				writer.close();
				br.close();
				System.out.println("Indexing Complete");
				fileWriter.write("Indexing Complete");
				fileWriter.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void prepareAndIndexReview(Gson gson, Review review, Document ldoc,
			String lineInFile) {
		review = gson.fromJson(lineInFile, Review.class);
		System.out.println("Indexing document: " + review.getReview_id());
		ldoc.add(new StringField("USERID", review.getUser_id(), Field.Store.YES));
		ldoc.add(new TextField("REVIEWID", review.getReview_id(),
				Field.Store.YES));
		ldoc.add(new DoubleField("STARS", review.getStars(), Field.Store.YES));
		ldoc.add(new LongField("Date", review.getDate().getTime(),
				Field.Store.YES));
		ldoc.add(new TextField("TEXT", review.getText(), Field.Store.YES));
		ldoc.add(new TextField("TYPE", review.getType(), Field.Store.YES));
		ldoc.add(new StringField("BUSINESSID", review.getBusiness_id(),
				Field.Store.YES));

	}

	public void prepareAndIndexTip(Gson gson, Tip tip, Document ldoc,
			String lineInFile) {
		tip = gson.fromJson(lineInFile, Tip.class);
		System.out.println("Indexing document: " + tip.getUser_id() + " "
				+ tip.getBusiness_id());
		ldoc.add(new TextField("USERID", tip.getUser_id(), Field.Store.YES));
		ldoc.add(new TextField("TEXT", tip.getText(), Field.Store.YES));
		ldoc.add(new TextField("BUSINESSID", tip.getBusiness_id(),
				Field.Store.YES));
		ldoc.add(new IntField("LIKES", tip.getLikes(), Field.Store.YES));
		ldoc.add(new LongField("Date", tip.getDate().getTime(), Field.Store.YES));
		ldoc.add(new TextField("TYPE", tip.getType(), Field.Store.YES));

	}

	public void prepareAndIndexBusiness(Gson gson, Business business,
			Document ldoc, String lineInFile) {
		business = gson.fromJson(lineInFile, Business.class);
		System.out.println("Indexing document: " + business.getBusiness_id());
		ldoc.add(new StringField("BUSINESSID", business.getBusiness_id(),
				Field.Store.YES));
		ldoc.add(new TextField("FULL_ADDRESS", business.getFull_address(),
				Field.Store.YES));
		FieldType HoursField = new FieldType();
		HoursField.setOmitNorms(true);
		HoursField.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
		HoursField.setStored(true);
		HoursField.setTokenized(true);
		HoursField.setStoreTermVectors(true);
		HoursField.freeze();
		ldoc.add(new Field("HOURS", business.getHours().toString(), HoursField));
		ldoc.add(new IntField("OPEN", (business.isOpen() == true ? 1 : 0),
				Field.Store.YES));
		// DID NOT INDEX CATEGORIES IN THIS YET
		String categories = "";
		for (String str : business.getCategories()) {
			categories += str + ", ";
		}
		ldoc.add(new TextField("CATEGORIES", categories, Field.Store.YES));
		ldoc.add(new TextField("CITY", business.getCity(), Field.Store.YES));
		ldoc.add(new IntField("REVIEW_COUNT", business.getReview_count(),
				Field.Store.YES));
		ldoc.add(new TextField("NAME", business.getName(), Field.Store.YES));
		// DID NOT INDEX NEIGHBORHOODS
		ldoc.add(new DoubleField("LONGITUDE", business.getLongitutde(),
				Field.Store.YES));
		ldoc.add(new TextField("STATE", business.getState(), Field.Store.YES));
		ldoc.add(new DoubleField("STARS", business.getStars(), Field.Store.YES));
		ldoc.add(new DoubleField("LATITUDE", business.getLatitude(),
				Field.Store.YES));
		FieldType AttributesField = new FieldType();
		AttributesField.setOmitNorms(true);
		AttributesField.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
		AttributesField.setStored(true);
		AttributesField.setTokenized(true);
		AttributesField.freeze();
		ldoc.add(new Field("ATTRIBUTES", business.getAttributes().toString(),
				AttributesField));
		ldoc.add(new TextField("TYPE", business.getType(), Field.Store.YES));

	}

	public static void main(String[] args) {
		IndexData ed = new IndexData();
		ed.setIndexPath("C:\\searchproject\\yelp\\index");
		ed.setCorpusPath("/com/search/project/yelp/dataset/yelp_dataset_challenge_academic_dataset");
		ed.generateIndex("review");
		ed.generateIndex("business");
		// ed.generateIndex("tip");

	}

}
