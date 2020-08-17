package net.olivo.lc4j;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import cern.colt.list.IntArrayList;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

import java.io.IOException;
import java.io.EOFException;

/** <p>This class does <strong>agglomerative clustering</strong> on a given set
 * of documents.</p>
 *
 * <p>The distance measure used is the <strong>distance between the tables
 * of the n-grams</strong>.</p>
 *
 * @author Marco Olivo
 */
public class AgglomerativeClustering {
	/** Enable debugging? */
	private static final boolean DEBUG = true;
	/** Default buffer size. */
	private static final int BUFFER_SIZE = 16 * 1024;

	/** The main loop.
	 *
	 * @param args the arguments.
	 * @throws IOException if some IO error occurs.
	 */
	public static void main( String args[] ) throws IOException {
		int ch;
		String path = null;
		LanguageCategorization lc = new LanguageCategorization();

		LongOpt[] longopts = new LongOpt[] {
				new LongOpt( "help", LongOpt.NO_ARGUMENT, null, 'h' )
		};

		Getopt g = new Getopt( "AgglomerativeClustering", args, "h", longopts );
		g.setOpterr( true );

		if ( args.length < 1 ) {
			System.err.println( "The path where the files to be clustered are must be specified on the command line." );
			System.err.println( "See help for more details on usage." );
			return ;
		}

		while ( ( ch = g.getopt() ) != -1 ) switch ( ch ) {
			case 'h':
				System.err.println( "Usage: AgglomerativeClustering [OPTIONS] path" );
				System.err.println( "Cluster the files found in the given path." );
				System.err.println( "" );
				System.err.println( "Required arguments:" );
				System.err.println( "  path                           the path where the files to be clustered are" );
				System.err.println( "" );
				System.err.println( "Help:" );
				System.err.println( "  -h, --help                     print this help screen" );
				System.err.println( "" );
				return;

			case '?':
				return;

			default:
				break;
		}

		path = args[0];

		if ( DEBUG ) System.err.println( "loading language models from files in " + path );

		File[] files = new File( path ).listFiles();
		final int n = files.length;

		// create language-models from files (one for each file)
		LanguageModel[] lm = new LanguageModel[n];
		for ( int i = 0; i < n; i++ ) {
			BufferedReader input = new BufferedReader( new FileReader( files[i] ) );

			lm[i] = lc.createLanguageModel( input );
		}
		if ( DEBUG ) System.err.println( "all language-models loaded" );

		// list of pointers to the clusters at the i-th step of the algorithm
		List currentClusters = new ArrayList();
		// number of clusters at the i-th step of the algorithm
		int numClusters = n;
		// how many n-grams should we consider when merging two language-models?
		int useTopmostNgrams = lc.getUseTopmostNgrams();
		/* where we keep distances between clusters; position (i, j) indicates the distance
		 * between the i-th cluster and the j-th cluster; if (i, j) is set to -1, it indicates
		 * that either i or j are non-existent clusters (in this iteration)
		 */
		int[][] distance = new int[n][n];

		for ( int i = 0; i < n; i++ ) Arrays.fill( distance[i], 0 );

		// stores the element numbers that make up each cluster
		IntArrayList[] clusters = new IntArrayList[n];

		// start the real thing - i.e., the agglomerative clustering
		while ( numClusters > 1 ) {
			final long startTime = System.currentTimeMillis();

			// at first step create data structures
			if ( numClusters == n ) {
				for ( int i = 0; i < n; i++ ) {
					for ( int j = i + 1; j < n; j++ ) {
						distance[i][j] = lc.calcDistance( lm[i], lm[j] );
						if ( DEBUG ) System.err.println( "initializing distance <" + i + "," + j + ">: " + distance[i][j] );
					}

					clusters[i] = new IntArrayList();
					clusters[i].add( i );
				}
				if ( DEBUG ) System.err.println( "all distances initialized" );
			}

			// find the two most similar clusters...
			int minI = 0, minJ = 0;
			int minDistance = Integer.MAX_VALUE;
			for ( int i = 0; i < n; i++ ) {
				for ( int j = i; j < n; j++ ) if ( distance[i][j] > 0 && lm[i] != null && lm[j] != null && distance[i][j] < minDistance ) {
					minDistance = distance[i][j];
					minI = i;
					minJ = j;
				}
			}
			if ( DEBUG ) System.err.println( "minimal distance found between <" + minI + "," + minJ + ">: " + minDistance );

			// ... and merge them
			lm[minI] = LanguageModel.merge( lm[minI], lm[minJ], useTopmostNgrams );
			clusters[minI].addAllOf( clusters[minJ] );

			lm[minJ] = null;
			for ( int i = 0; i < n; i++ ) distance[i][minJ] = -1;	// useless?
			clusters[minJ].clear();

			/* now, calculate the distances between the new cluster (formed by the merging of the
			 * two most similar clusters) and all the other clusters
			 */
			for ( int j = 0; j < n; j++ ) if ( lm[j] != null && j != minI ) {
				distance[minI][j] = lc.calcDistance( lm[minI], lm[j] );
			}

			final long endTime = System.currentTimeMillis();

			if ( DEBUG ) {
				System.err.println( "step " + ( n - numClusters + 1 ) + ": merging clusters <" + minI + "," + minJ + ">" );
				System.err.println( "time taken: " + (double)( endTime - startTime ) / 1000 + "s" );
			}

			// print out non-empty clusters to stdout
			System.out.println( "step " + ( n - numClusters + 1 ) + " - " + ( numClusters - 1 ) + " clusters left:" );
			for ( int i = 0; i < n; i++ ) {
				if ( clusters[i].size() != 0 ) {
					System.out.print( "\t" );
					for ( int j = 0; j < clusters[i].size(); j++ ) {
						System.out.print( "<" + files[clusters[i].get( j )].getName() + "> " );
					}
					System.out.println( "" );
				}
			}
			System.out.println( "***" );

			// update cluster count
			numClusters--;
		}
	}
}
