package com.medavox.util.image;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;


import com.medavox.util.io.RecurseOperation;

/* Unique Colour Metric

 Definition:
    the number of unique colours in an image, divided by the number of pixels
 
 Theory:
    images with a Unique Colour Metric (UCM) below a threshold (what threshold?) 
    are actually drawings, (phone) screenshots, and those inspirational quotes/paragraphs on a solid background.
    AKA non-photos

   Exceptions to this rule:
 * black and white photos: unusually low UCM. Detectable
 * dark images? lots of black pixels surrounding a photo
 * photos with a high noise level: unusually high UCM. These don't matter, they are still above threshold
 
   Other Tests:
 * average brightness of image
 * number of pixels which are the most common colour
     * PERCENTAGE of image's pixels which are the most common colour (or top 3 colours)
 * test whether image is greyscale (effectively, not encoding)
 * difference between a pixel and its neighbours (tests against solid colour areas)
 * a potential way to create a hash of an image whose hamming distance 
 * to similar images relates to its actual similarity to other images,
 *  can think of an image as a serious of coordinates in 3D (RGB) or 4D
 *  (ARGB) space. Each pixel is a point in that space, and the series of
 * the images pixels, from top-left to bottom-right describes its movements in time*/
public class ImageAnalyser
{
    private BufferedImage image;
    private int w;
    private int h;
    private int pixels;
    private File f;
    //private int[][] histogram;//todo: only calc histogram once, and return this afterwards
    public ImageAnalyser(File f)
    {
        this.f = f;
    }
    
    public boolean setup()
    {
        try
        {
            image = ImageIO.read(f);
            w = image.getWidth();
            h = image.getHeight();
            pixels = w*h;
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.err.println("causing image: "+f);
            //System.exit(1);
            return false;
        }
    }
    /**Coming Soon...*/
    /*private int identicalNeighbours()
    {
        //boolean[][] identicals = new boolean[w][h];
        for(int y = 0; y < h; y++)
        {
            for(int x = 0; x < w; x++)
            {
                
                if(x < w-1) //this isn't the rightmost pixel on this row
                {
                    boolean identRight = (image.getRGB(x,y) == image.getRGB(x+1,y));
                }
                
                if(y < w-1)
                {
                    boolean identDown = (image.getRGB(x,y) == image.getRGB(x,y+1));
                }
            }
        }
    }*/
    
    /**Problem:
     * 
     * as images get higher and higher resolution, the number of unique 
     * coloured pixels in them doesn't really scale up alongside this.
     * 
     * This means higher resolution photographs (which don't have 
     * more colours than a lower-res equivalent) will have a lower UCM.*/
    
    /**getDistinctImages(): tells you the number of unique colours,
     * where colours within a certain distance threshold are counted as the same */
    
    /**treat animage as a linear series of up and down values, which modify an end number.
     * the rough value at the end gives a feel of the series of brights and darks in the image*/
     
     /**image modifications:
      * scaling
      * rotation
      *     simple (90 deg)
      *     complex
      * colour shift
      * cropping
      * covering (other content goes on top)*/
    
    public int[] getRGB(int pixel)
    {
        int red   = (pixel & 0x00ff0000) >> 16;
        int green = (pixel & 0x0000ff00) >> 8;
        int blue  =  pixel & 0x000000ff;
        int[] ret = {red, green, blue};
        return ret;
    }
    
    public int[] pixelDistance(int pixelA, int pixelB)
    {
        int[] rgb1 = getRGB(pixelA);
        int[] rgb2 = getRGB(pixelB);
        int[] diffs = new int[3];
        for(int i = 0; i < 3; i++)
        {
            diffs[i] = Math.abs(rgb1[i]-rgb2[i]);
        }
        
        return diffs;
    }
    
    public int simpleDistance(int pixelA, int pixelB)
    {
        int[] diffs = pixelDistance(pixelA, pixelB);
        return Math.max(diffs[0], Math.max(diffs[1], diffs[2]));
    }
    
    public boolean similarEnough(int pixelA, int pixelB, int threshold)
    {
        return simpleDistance(pixelA, pixelB) <= threshold;
    }
    /*
    Map<Integer, Integer> mergeSimilarColours(Map<Integer, Integer> in, int threshold)
    {
        
    }*/
    
    public int pixelsWhichAreTopNColours(int[][] histo, int topNum)
    {
        if(topNum > histo[0].length)
        {
            topNum = histo[0].length;
        }
        int sum = 0;
        for(int i = histo[0].length-1; i >= histo[0].length - topNum; i--)
        {
            sum+= histo[1][i];
        }
        return sum;
    }
    
    public int getPixels()
    {
        return pixels;
    }
    
    public Set<Integer> getUniqueColours()
    {
        Set<Integer> colours = new HashSet<Integer>();
        for(int y = 0; y < h; y++)
        {
            for(int x = 0; x < w; x++)
            {
                int pixel = image.getRGB(x, y);
                colours.add(pixel);
            }
        }
        return colours;
    }
    //oh ma DAZE der type ano tesjuns!
    private List<Map.Entry<Integer,Integer>> sortByValue(Map<Integer, Integer> map) 
    {
        List<Map.Entry<Integer,Integer>> list =
            new ArrayList<Map.Entry<Integer,Integer>>(map.entrySet());
        Collections.sort(list,
            new Comparator<Map.Entry<Integer,Integer>>()
            {
                public int compare(Map.Entry<Integer,Integer> o1, Map.Entry<Integer,Integer> o2)
                {
                    return ( (Comparable<Integer>)((Map.Entry<Integer,Integer>)o1).getValue())
                    .compareTo(( (Map.Entry<Integer,Integer>)o2).getValue());
                }
            });
        /*
        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry)it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;*/
        return list;
    }

    public Map<Integer, Integer> getHistogram()
    {
        Map<Integer, Integer> colourFreqs = new HashMap<Integer, Integer>();
        for(int y = 0; y < h; y++)
        {
            for(int x = 0; x < w; x++)
            {
                Integer pixel = new Integer(image.getRGB(x, y));
                //colours.add(pixel);
                Integer freq = colourFreqs.get(pixel);
                if(freq == null)
                {
                    freq = new Integer(1);
                }
                else
                {//incrementing reference Integer objects is ugly
                    colourFreqs.put(pixel, new Integer(freq.intValue() + 1));
                }
            }
        }
        return colourFreqs;
    }

    
    public boolean isGreyscale(int[] uCols)
    {
        for(int pixel : uCols)
        {
            int red   = (pixel & 0x00ff0000) >> 16;
            int green = (pixel & 0x0000ff00) >> 8;
            int blue  =  pixel & 0x000000ff;
        
            if(red != blue || red != green)
            {
                return false;
            }
        }
        return true;
    }
    
    public boolean isGrayscale(int[] uCols)
    {
        return isGreyscale(uCols);
    }
    
    public int numPixelsAboveThresholdBrightness(int threshold)
    {
        int sum = 0;
        for(int y = 0; y < h; y++)
        {
            for(int x = 0; x < w; x++)
            {
                int pixel = image.getRGB(x, y);
                if(getBrightness(pixel) > threshold)
                {
                    sum++;
                }
            }
        }
        return sum;
    }
    
    public double avgBrightness()
    {
        long sum = 0;
        for(int y = 0; y < h; y++)
        {
            for(int x = 0; x < w; x++)
            {
                int pixel   = image.getRGB(x, y);
                int bryt    = getBrightness(pixel);
                sum += bryt;
            }
        }
        return (double)sum / pixels;
    }
    
    private int getBrightness(int pixel)
    {
        int r       = (pixel & 0x00ff0000) >> 16;
        int g       = (pixel & 0x0000ff00) >> 8;
        int b       =  pixel & 0x000000ff;
        
        int cmax = (r > g) ? r : g;
        if (b > cmax) cmax = b;
        
        return cmax;
    }
}

class ColourEntry
{
    public int colour;
    public int frequency;
    
    public ColourEntry(int colour, int frequency)
    {
        this.colour = colour;
        this.frequency = frequency;
    }
}
