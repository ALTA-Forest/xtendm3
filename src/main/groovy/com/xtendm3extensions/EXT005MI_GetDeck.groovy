// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get a deck from EXTDPH
// Transaction GetDeck
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DPNA - Deck Name
 * @param: SORT - Sort Code
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DPNA - Deck Name
 * @return: SORT - Sort Code
 * @return: YARD - Yard
 * @return: MBFW - Weight of 1 MBF
 * @return: DPDT - Deck Date
 * @return: DPLC - Life Cycle
 * @return: DPID - Deck ID
 * 
*/


public class GetDeck extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDPID
  
  // Constructor 
  public GetDeck(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database  
     this.program = program
     this.logger = logger
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     inDIVI = mi.in.get("DIVI")
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Deck ID
     if (mi.in.get("DPID") != null) {
        inDPID = mi.in.get("DPID") 
     } else {
        inDPID = 0         
     }
 
    
     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTDPH record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTDPH").index("00").selectAllFields().build()
     DBContainer EXTDPH = action.getContainer()
     EXTDPH.set("EXCONO", inCONO)
     EXTDPH.set("EXDIVI", inDIVI)
     EXTDPH.set("EXDPID", inDPID)
     
    // Read  
    if (action.read(EXTDPH)) { 
      mi.outData.put("CONO", EXTDPH.get("EXCONO").toString())
      mi.outData.put("DIVI", EXTDPH.getString("EXDIVI"))
      mi.outData.put("DPID", EXTDPH.get("EXDPID").toString()) 
      mi.outData.put("DPNA", EXTDPH.getString("EXDPNA")) 
      mi.outData.put("TYPE", EXTDPH.getString("EXTYPE")) 
      mi.outData.put("SORT", EXTDPH.getString("EXSORT")) 
      mi.outData.put("YARD", EXTDPH.getString("EXYARD")) 
      mi.outData.put("MBFW", EXTDPH.getDouble("EXMBFW").toString()) 
      mi.outData.put("DPDT", EXTDPH.get("EXDPDT").toString()) 
      mi.outData.put("DPLC", EXTDPH.get("EXDPLC").toString()) 
      mi.write() 
    } else {
      mi.error("No record found")   
      return 
    }
  } 
  
}