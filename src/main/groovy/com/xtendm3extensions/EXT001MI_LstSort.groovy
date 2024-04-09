// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list sorts from EXTSOR
// Transaction LstSort
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SORT - Sort Code
 * @param: ITNO - Item Number
 * 
*/

/**
 * OUT
 * @return : CONO - Company
 * @return : SORT - Sort Code
 * @return : SONA - Name
 * @return : ITNO - Item Number 
 * 
*/


public class LstSort extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  int inCONO
  String inSORT
  String inITNO
  
  // Constructor 
  public LstSort(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database 
     this.program = program
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = program.LDAZD.CONO as Integer
    
     // Sort Code
     if (mi.in.get("SORT") != null && mi.in.get("SORT") != "") {
        inSORT = mi.inData.get("SORT").trim() 
     } else {
        inSORT = ""     
     }

     // Item Number
     if (mi.in.get("ITNO") != null && mi.in.get("ITNO") != "") {
        inITNO = mi.inData.get("ITNO").trim() 
     } else {
        inITNO = ""     
     }

     // List Sorts from EXTSOR
     listSorts()
  }
 

  //******************************************************************** 
  // List sorts from EXTSOR
  //******************************************************************** 
  void listSorts(){ 
     // Read with both SORT and ITNO as keys if entered 
     if (inSORT != "" && inITNO != "") {  
        DBAction action = database.table("EXTSOR").index("10").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
        ext.set("EXCONO", inCONO)
        ext.set("EXITNO", inITNO)
        ext.set("EXSORT", inSORT)

        int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	 
        action.readAll(ext, 3, pageSize, releasedItemProcessor) 

     } else if (inSORT != "" && inITNO == "") {
        DBAction action = database.table("EXTSOR").index("00").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
        ext.set("EXCONO", inCONO)
        ext.set("EXSORT", inSORT)
        
        if (action.read(ext)) {  
          mi.outData.put("CONO", ext.get("EXCONO").toString())
          mi.outData.put("SORT", ext.getString("EXSORT"))
          mi.outData.put("SONA", ext.getString("EXSONA"))
          mi.outData.put("ITNO", ext.getString("EXITNO"))
          mi.write()
        }

     } else if (inITNO != "" && inSORT == "") {
        DBAction action = database.table("EXTSOR").index("10").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
        ext.set("EXCONO", inCONO)
        ext.set("EXITNO", inITNO)
        
        int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	 
        action.readAll(ext, 2, pageSize, releasedItemProcessor) 
        
     } else {
        DBAction action = database.table("EXTSOR").index("00").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
        ext.set("EXCONO", inCONO)

        int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	 
        action.readAll(ext, 1, pageSize, releasedItemProcessor) 
     } 
  } 

    Closure<?> releasedItemProcessor = { DBContainer ext -> 
      mi.outData.put("CONO", ext.get("EXCONO").toString())
      mi.outData.put("SORT", ext.getString("EXSORT"))
      mi.outData.put("SONA", ext.getString("EXSONA"))
      mi.outData.put("ITNO", ext.getString("EXITNO"))
      mi.write() 
   } 
}