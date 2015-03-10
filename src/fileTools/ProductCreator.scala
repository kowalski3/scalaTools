package fileTools

import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import java.io._
import myUtils.MyFileUtils


/*
 * FORMATS
 *  xml (mp3 & xml)
 *  mp4 (hd on new source)
 *  mov (hd on old source
 *  mp3g zip (320 on old source)
 *  mp3g (128 on old course, 320 on new)
 */


//TO DO - Fix -> mixing java and scala io here.
//TO DO - Check data for special characters like , that might cause java.io.FileNotFoundException
//TO DO - To run quickly needs to map harddrive as it goes
class ProductCreator(
                      val sourceDir: File, 
                      val destDirName: String) {
    
    val albumMap = Map[String,Album]()
  
    
  /**
   * Constructor
   */
  def this(sourceDirName: String, destDirName: String, dataFileName: String) = {
    
    this(new File (sourceDirName),destDirName)
    val dataFile = scala.io.Source.fromFile(dataFileName)
    
    for (lines <- dataFile.getLines()) {
       // Get values from line
       val line = lines.split(",") 
       val albumid:String = line(0)
       val sfid:String = line(1) //need better of handling this
       val fileName:String = line(2)
     
       // Check if album already in map and add if not
       val result = albumMap.get(albumid)
       result match{
         case None => albumMap.put(albumid, new Album(albumid))        
         case Some(x) =>
       }
       albumMap.get(albumid).get.addTrack(sfid, fileName)
    } 
    
  }//constructor end
  
  
  /**
   *   Returns a list of file prefixes 
   */
  def getFileNames(albumid:String):String = {
    val result = albumMap.get(albumid)
       result match{
         case None =>    throw new RuntimeException("Error " + albumid + " cannot be found")
         case Some(x) => result.get.getAlbumIds.toString()  
       }
    
  }
  
  /**
   * Create Product
   */
    def createProduct(albumid: String, format: String) = {
      
      val suffixes = getFormat(format)
      
      val result = albumMap.get(albumid)
      result match{
         case None => println("Product code " + albumid + " doesn't exist.")
         case Some(album) => 
           for ((songid,trackName) <- result.get.trackList)
               try{
                 println(songid)
                copyFiles(songid, suffixes, trackName) 
               } catch{
                 case e: RuntimeException => println(e)
               }     
       }
      
     }
    
    def getFormat(format:String):ListBuffer[String] = {
       val suffixes = new ListBuffer[String]
      format match {
        case "xml" => suffixes += ".xml"  += ".mp3"
        case "mp4" => suffixes += ".mp4"
        case "mov" => suffixes += ".mov"
        case "mp3g zip" => suffixes += ".zip"
        case "mp3g " => suffixes += ".cdg" += "mp3"
        case _ => throw new IllegalArgumentException(format + " not valid. Valid options are xml / mp4 / mov / mp3g zip / mp3g ")
      }
    }
    
    
    /**
     * Copy files
     * Traverses the track in the album and calls copyFile for eachTrack
     * 
     */
    def copyFiles(songid:String, suffixes:ListBuffer[String], newTrackName:String) {
     
      for(suffix <- suffixes) copyFile(songid, suffix, newTrackName)
    }

    
    /**
     * Copy file
     */
   def copyFile(songid:String, suffix:String, newTrackName:String) {
     
    for(f <- MyFileUtils.walkTree(sourceDir)){
      if (f.getName.contains(songid) && (f.getName.endsWith(suffix))){
          f
        MyFileUtils.copyFile(f, new File(destDirName + "/" + newTrackName + suffix))
          return
      }
    }
    throw new RuntimeException("Warning " + songid + " in " + suffix + " format not found in " + sourceDir)
   }

}


/**
 * Runner object
 * 
 */
object ProductCreatorRun extends App {
      val sourceDirName = "C:/Users/Julian.SUNFLYKARAOKE/Desktop/testSrc"
      val destDirName = "C:/Users/Julian.SUNFLYKARAOKE/Desktop/testDest"
      val dataFileName = "C:/Julian/git/scalaTools/data/ProductCreatorData.csv"
      val x = new ProductCreator(sourceDirName, destDirName, dataFileName)
      println(x.getFileNames("SF349"))
      x.createProduct("SF349", "xml")  
}