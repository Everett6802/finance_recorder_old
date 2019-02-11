package com.price.finance_recorder_rest.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.common.CmnFunc;


public class CmnClass
{
	public static class NonBlockingProcessBuilder implements Runnable
	{
		
		private String cmd_string = null;
		private Thread observe_thread = null;
		private String observe_log_filepath = null;
		
		public NonBlockingProcessBuilder(String cmd)
		{
			cmd_string = cmd;
		}
		
		public void start()
		{
			observe_thread = new Thread(this);
			observe_thread.start();
		}
		
		public boolean is_done()
		{
			// assert(observe_thread != null )
			return !observe_thread.isAlive();
		}
		
		@Override
		public void run()
		{
			ProcessBuilder builder = new ProcessBuilder(cmd_string.split(" "));
			builder.redirectErrorStream(true);
			try
			{
				// Log file
				File file = new File(observe_log_filepath);
				file.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
				// Start to run the process
				Process p = builder.start();
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null;
				// Read the output data from the process and write into the log
				while ((line = br.readLine()) != null)
				{
					bw.write(line);
					// System.out.println(line);
				}
				bw.flush();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

/*
As a more efficient solution, you could write a class that presents a reversed view of a List 
as an Iterable generic. The iterator returned by your class would use the ListIterator of the 
decorated list to walk over the elements in reverse order.
 */
	public static class Reversed<T> implements Iterable<T> 
	{
	    private final List<T> original;

	    public Reversed(List<T> original) 
	    {
	        this.original = original;
	    }

		@Override
	    public Iterator<T> iterator() 
	    {
	        final ListIterator<T> i = original.listIterator(original.size());

	        return new Iterator<T>() 
	        {
	            public boolean hasNext() { return i.hasPrevious(); }
	            public T next() { return i.previous(); }
	            public void remove() { i.remove(); }
	        };
	    }

	    public static <T> Reversed<T> reversed(List<T> original) 
	    {
	        return new Reversed<T>(original);
	    }
	}
}
