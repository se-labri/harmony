/*
 * Copyright (c) 2013 TMate Software Ltd
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For information on how to redistribute this software under
 * the terms of a license other than GNU General Public License
 * contact TMate Software at support@hg4j.com
 */
package org.tmatesoft.hg.console;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.tmatesoft.hg.core.Nodeid;
import org.tmatesoft.hg.core.SessionContext;
import org.tmatesoft.hg.internal.BasicSessionContext;
import org.tmatesoft.hg.repo.HgChangelog;
import org.tmatesoft.hg.repo.HgLookup;
import org.tmatesoft.hg.repo.HgParentChildMap;
import org.tmatesoft.hg.repo.HgRepository;


/**
 * 
 * @author Artem Tikhomirov
 * @author TMate Software Ltd.
 */
public class Whip {
	
	// mem-map:on. 13-15 seconds
	// getBranches: 15679 ms. Parent-child map: 478 ms
	// mem-map:off. 11-15 seconds
	//
	// mem-map:off, directChildren for each revision:
	// HgParentChildMap.init: 521 ms. 
	// HgParentChildMap.directChildren: 11319 ms. 9 heads (expected 9); erroneous heads: 0
	// Total comparisons: 2 992 878 432, average: 38683
	// (38k^2 = 1.5 * 10^9)
	// getBranches: 3750 ms. 

	public static void main(String[] args) throws Exception {
		final File repoLoc = new File("/home/artem/hg/cpython/");
		SessionContext ctx = new BasicSessionContext(Collections.singletonMap("hg4j.dap.mapio_limit", 0), null);
		HgRepository repo = new HgLookup(ctx).detect(repoLoc);

//		final long start1 = System.currentTimeMillis();
//		final HgParentChildMap<HgChangelog> pw = new HgParentChildMap<HgChangelog>(repo.getChangelog());
//		pw.init();
//		final long end1 = System.currentTimeMillis();
//		System.out.printf("HgParentChildMap.init: %d ms. \n", end1-start1);
		
//		int err = 0, heads = 0, secondParentCount = 0;
//		final long start3 = System.currentTimeMillis();
//		pw.AAA = 0;
//		for (Nodeid n : pw.all()) {
//			List<Nodeid> dc = pw.directChildren(n);
//			if (dc.isEmpty()) {
//				heads++;
////				if (pw.hasChildren(n)) {
////					err++;
////				}
//			}
////			if (pw.secondParent(n) != null) {
////				secondParentCount++;
////			}
//		}
//		final long end3 = System.currentTimeMillis();
//		System.out.printf("HgParentChildMap.directChildren: %d ms. %d heads (expected %d); erroneous heads: %d \n", end3-start3, heads, pw.heads().size(), err);
//		System.out.printf("Total comparisons: %d, average: %d", pw.AAA, pw.AAA / pw.all().size());
//		System.out.printf("Second parent != null: %d\n", secondParentCount);
		

		new File(repoLoc, ".hg/cache/branchheads").delete();
		final long start2 = System.currentTimeMillis();
		repo.getBranches();
		final long end2 = System.currentTimeMillis();
		System.out.printf("getBranches: %d ms. \n", end2-start2);
	}

}
