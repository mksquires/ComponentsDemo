package agi.examples.utilities;

import java.awt.Color;

public class ColorGradient{

    /**
     * Gets the color. Maps doubles between [base value, top value]
     * Maps colors between green and magenta. Doubles outside the range are clamped.
     * @param valueToMapToColor the value to map to a color
     * @return the color
     */
    public Color GetColor(double valueToMapToColor){

        float[] baseColorSpace = m_baseColor.getRGBColorComponents(null);
        float[] topColorSpace = m_topColor.getRGBColorComponents(null);
        float[] outputColor = new float[topColorSpace.length];

        // clamped 0,1
        float ratio = (float)Math.max(0, Math.min(1, (valueToMapToColor-m_baseValue)/(m_topValue-m_baseValue)));
        
        float deltaR = topColorSpace[0] - baseColorSpace[0];
        float deltaG = topColorSpace[1] - baseColorSpace[1];
        float deltaB = topColorSpace[2] - baseColorSpace[2];

        outputColor[0] = baseColorSpace[0] + ratio*deltaR;
        outputColor[1] = baseColorSpace[1] + ratio*deltaG;
        outputColor[2] = baseColorSpace[2] + ratio*deltaB;     

        return new Color(outputColor[0], outputColor[1], outputColor[2], (float)1.0);
    }
    
    /**
     * Gets the color. Maps doubles between [base value, top value]
     * Maps colors between orange and magenta. Doubles outside the range are clamped
     * @param valueToMapToColor the value to map to a color
     * @return the color
     */
    public Color GetBucketedColor(double valueToMapToColor){

        int bucket = (int)(valueToMapToColor/m_bucketSize);
        bucket = bucket < 0 ? 0 : bucket;
        bucket = bucket > m_colorBuckets.length-1 ? m_colorBuckets.length-1 : bucket;

        return m_colorBuckets[bucket];
    }   

    /**
     * Initializes this class with the specified number of color buckets.
     * From orange -> magenta. from 0.0 -> 10,000
     * @param bucketCount The number of color buckets.
     */
    public ColorGradient(int bucketCount ){
        this(bucketCount, Color.ORANGE, Color.MAGENTA, (float)0.0, (float)10000.0);
    }


    /**
     * Initializes this class with the specified parameters.
     * @param bucketCount The number of color buckets.
     * @param baseColor the color to use at the base value
     * @param topColor the color to use at the top value
     * @param baseValue the value of the base of the range
     * @param topValue the value ofthe top of the range
     */
    public ColorGradient(int bucketCount, Color baseColor, Color topColor, float baseValue, float topValue ){
        m_baseColor = baseColor;
        m_topColor = topColor;
        m_baseValue = baseValue;
        m_topValue = topValue;

        float[] baseColorSpace = baseColor.getRGBColorComponents(null);
        float[] topColorSpace = topColor.getRGBColorComponents(null);
        float[] outputColor = new float[topColorSpace.length];
        m_bucketSize = (topValue -baseValue)/(float)bucketCount;

        float deltaR = topColorSpace[0] - baseColorSpace[0];
        float deltaG = topColorSpace[1] - baseColorSpace[1];
        float deltaB = topColorSpace[2] - baseColorSpace[2];

        m_colorBuckets = new Color[bucketCount];

        for(int i = 0; i < bucketCount; i++){
            float ratio = (float)i/(float)bucketCount;
            outputColor[0] = baseColorSpace[0] + ratio*deltaR;
            outputColor[1] = baseColorSpace[1] + ratio*deltaG;
            outputColor[2] = baseColorSpace[2] + ratio*deltaB;   

            m_colorBuckets[i] = new Color(outputColor[0], outputColor[1], outputColor[2], (float) 0.75);
        } 
    }

    /**
     * Prints the color gradient out to System.out
     */
    public void PrintInformationToSystemOut(){
        System.out.println("base value " + m_baseValue + " base color " + m_baseColor );
        System.out.println("top value " + m_topValue + " top color " + m_topColor);
        System.out.print("bucket size " + m_bucketSize + " number of buckets " + m_colorBuckets.length + "\n");
        for(int i = 0; i < m_colorBuckets.length; i++){
            String hex = String.format("#%02x%02x%02x", m_colorBuckets[i].getRed(),m_colorBuckets[i].getGreen(), m_colorBuckets[i].getBlue() );  
            System.out.print(hex + " bucket " + i + "\n");
        }
    }

    private float m_baseValue;
    private float m_topValue;
    private Color m_baseColor;
    private Color m_topColor;
    
    private float m_bucketSize;
    private Color[] m_colorBuckets;
}