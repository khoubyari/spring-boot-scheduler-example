package com.khoubyari.example.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.khoubyari.example.dto.Comment;

/* 
 * a sample scheduled task. Spring scheduled is not cluster-friendly. If you are running a cluster
 *  you have to enable scheduling in one app instance and disable it in all others. (see application.yml) 
 */

@Component
public class CommentPullerScheduledTask {

	private static final Logger log = LoggerFactory
			.getLogger(CommentPullerScheduledTask.class);

	// set this to false to disable this job; set it it true by
	@Value("${example.scheduledJob.enabled:false}")
	private boolean scheduledJobEnabled;
	
	@Value("${example.incoming.comments.dir}")
	private String commentsDir;

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"HH:mm:ss");

	@Scheduled(fixedRate = 30000)  // every 30 seconds
	public void pullRandomComment() {
		if (!scheduledJobEnabled) {
			return;
		}
		int min = 1;
		int max = 500;
		Random r = new Random();
		int id = r.nextInt((max - min) + 1) + min;
		String outFileName = createDataFile(id);
		RestTemplate restTemplate = new RestTemplate();
		// pull data from example REST API
		Comment comment = restTemplate.getForObject("http://jsonplaceholder.typicode.com/comments/" + id, Comment.class);
		log.info("Pulled comment #" + id + " at " + dateFormat.format(new Date()));
		log.info("Writing to " + outFileName);
		
		 try(PrintStream ps = new PrintStream(outFileName)) { ps.println( comment.toString()); }
		 catch (FileNotFoundException e) {
			 log.error("Couldn't write comment file.", e);
		 }
	}

	private String createDataFile(int id) {

		File dir = new File(commentsDir);
		if (!dir.exists()) {
			try {
				dir.mkdir();
			} catch (SecurityException se) {
				throw se;
			}
		}

		String name = "comment_" + String.format("%1$03d.", id)
				+ new Date().getTime();
		return dir.getAbsolutePath() + File.separator + name;
	}

	// examples of other CRON expressions
	// * "0 0 * * * *" = the top of every hour of every day.
	// * "*/10 * * * * *" = every ten seconds.
	// * "0 0 8-10 * * *" = 8, 9 and 10 o'clock of every day.
	// * "0 0/30 8-10 * * *" = 8:00, 8:30, 9:00, 9:30 and 10 o'clock every day.
	// * "0 0 9-17 * * MON-FRI" = on the hour nine-to-five weekdays
	// * "0 0 0 25 12 ?" = every Christmas Day at midnight

}
