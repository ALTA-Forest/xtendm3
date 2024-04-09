// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list species from EXTSPE
// Transaction LstSpecies
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CATE - Category
 * @param: SPEC - Species 
 * 
*/

/**
 * OUT
 * @return : CONO - Company
 * @return : CATE - Category
 * @return : SPEC - Species
 * @return : SPNA - Name
 * @return : SORT - Sort Code
 * 
*/


public class LstSpecies extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  int inCONO
  String inCATE
  String inSPEC
  String inSORT
  
  // Constructor 
  public LstSpecies(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database 
     this.program = program
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = program.LDAZD.CONO as Integer
    
     // Category
     if (mi.in.get("CATE") != null && mi.in.get("CATE") != "") {
        inCATE = mi.inData.get("CATE").trim() 
     } else {
        inCATE = ""     
     }
     
     // Species
     if (mi.in.get("SPEC") != null && mi.in.get("SPEC") != "") {
        inSPEC = mi.inData.get("SPEC").trim() 
     } else {
        inSPEC = ""     
     }

     // Sort
     if (mi.in.get("SORT") != null && mi.in.get("SORT") != "") {
        inSORT = mi.inData.get("SORT").trim() 
     } else {
        inSORT= ""     
     }


     // List Species from EXTSPE
     listSpecies()
  }
 
    
  //******************************************************************** 
  // List species from EXTSPE
  //******************************************************************** 
  void listSpecies(){ 
     // Read with all three fields CATE, SPEC and SORT as keys if entered 
     if (inSPEC != "" && inCATE != "" && inSORT != "") {  
        DBAction action = database.table("EXTSPE").index("20").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
        ext.set("EXCONO", inCONO)
        ext.set("EXCATE", inCATE)
        ext.set("EXSPEC", inSPEC)
        ext.set("EXSORT", inSORT)

        int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	 
        action.readAll(ext, 4, pageSize, releasedItemProcessor) 

     } else if (inSPEC != "" && inCATE == "" && inSORT == "") {
        DBAction action = database.table("EXTSPE").index("10").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
        ext.set("EXCONO", inCONO)
        ext.set("EXSPEC", inSPEC)
        
        int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	 
        action.readAll(ext, 2, pageSize, releasedItemProcessor) 
        
     } else if (inSPEC != "" && inCATE != "" && inSORT == "") {
        DBAction action = database.table("EXTSPE").index("00").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
        ext.set("EXCONO", inCONO)
        ext.set("EXCATE", inCATE)
        ext.set("EXSPEC", inSPEC)
     
        if (action.read(ext)) {  
          mi.outData.put("CONO", ext.get("EXCONO").toString())
          mi.outData.put("CATE", ext.getString("EXCATE"))
          mi.outData.put("SPEC", ext.getString("EXSPEC"))      
          mi.outData.put("SPNA", ext.getString("EXSPNA"))
          mi.outData.put("SORT", ext.getString("EXSORT"))
          mi.write()  
        }
        
     } else if (inCATE != "" && inSPEC == "" && inSORT == "") {
        DBAction action = database.table("EXTSPE").index("00").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
        ext.set("EXCONO", inCONO)
        ext.set("EXCATE", inCATE)
        
        int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	 
        action.readAll(ext, 2, pageSize, releasedItemProcessor) 
        
     } else if (inCATE != "" && inSPEC == "" && inSORT != "") {
        DBAction action = database.table("EXTSPE").index("20").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
        ext.set("EXCONO", inCONO)
        ext.set("EXCATE", inCATE)
        ext.set("EXSORT", inSORT)
        
        int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	 
        action.readAll(ext, 3, pageSize, releasedItemProcessor) 
        
     } else if (inCATE == "" && inSPEC == "" && inSORT != "") {
        DBAction action = database.table("EXTSPE").index("20").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
        ext.set("EXCONO", inCONO)
        ext.set("EXSORT", inSORT)
        
        int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	 
        action.readAll(ext, 2, pageSize, releasedItemProcessor) 
        
     } else if (inCATE == "" && inSPEC != "" && inSORT != "") {
        DBAction action = database.table("EXTSPE").index("30").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
        ext.set("EXCONO", inCONO)
        ext.set("EXSORT", inSORT)
        ext.set("EXSPEC", inSPEC)

        int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	 
        action.readAll(ext, 3, pageSize, releasedItemProcessor) 
     } else {
        DBAction action = database.table("EXTSPE").index("00").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
        ext.set("EXCONO", inCONO)

        int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	 
        action.readAll(ext, 1, pageSize, releasedItemProcessor) 
     } 
  }

    Closure<?> releasedItemProcessor = { DBContainer ext -> 
      mi.outData.put("CONO", ext.get("EXCONO").toString())
      mi.outData.put("CATE", ext.getString("EXCATE"))
      mi.outData.put("SPEC", ext.getString("EXSPEC"))      
      mi.outData.put("SPNA", ext.getString("EXSPNA"))
      mi.outData.put("SORT", ext.getString("EXSORT"))
      mi.write() 
   } 
}