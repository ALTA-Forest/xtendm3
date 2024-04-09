// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list supplier brand from EXTSBR
// Transaction LstSupBrand
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7 

/**
 * IN
 * @param: BRND - Brand
 * 
*/

/**
 * OUT
 * @return : CONO - Company
 * @return : BRND - Brand
 * @return : SUNO - Supplier
 * 
*/


public class LstSupBrand extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  int inCONO
  String inBRND
  String inSUNO
  
  
  // Constructor 
  public LstSupBrand(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database 
     this.program = program
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = program.LDAZD.CONO as Integer
    
     // Brand
     if (mi.in.get("BRND") != null && mi.in.get("BRND") != "") {
        inBRND = mi.inData.get("BRND").trim() 
     } else {
        inBRND = ""     
     }
     
     // Supplier
     if (mi.in.get("SUNO") != null && mi.in.get("SUNO") != "") {
        inSUNO = mi.inData.get("SUNO").trim() 
     } else {
        inSUNO = ""     
     }

     // List Brands from EXTSBR
     listSupplierBrands()
  }
 

  //******************************************************************** 
  // List supplier brands from EXTSBR
  //******************************************************************** 
  void listSupplierBrands(){ 
     // Read with both BRND and SUNO as keys if entered 
     if (inBRND != "" && inSUNO != "") {  
        DBAction action = database.table("EXTSBR").index("00").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
        ext.set("EXCONO", inCONO)
        ext.set("EXSUNO", inSUNO)
        ext.set("EXBRND", inBRND)

        if (action.read(ext)) {  
          mi.outData.put("CONO", ext.get("EXCONO").toString())
          mi.outData.put("SUNO", ext.getString("EXSUNO"))
          mi.outData.put("BRND", ext.getString("EXBRND"))
          mi.write()  
        }

     } else if (inBRND != "" && inSUNO == "") {
        DBAction action = database.table("EXTSBR").index("10").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
        ext.set("EXCONO", inCONO)
        ext.set("EXBRND", inBRND)
        
        int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	 
        action.readAll(ext, 2, pageSize, releasedItemProcessor) 
        
     } else if (inSUNO != "" && inBRND == "") {
        DBAction action = database.table("EXTSBR").index("00").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
        ext.set("EXCONO", inCONO)
        ext.set("EXSUNO", inSUNO)
        
        int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	 
        action.readAll(ext, 2, pageSize, releasedItemProcessor) 
        
     } else {
        DBAction action = database.table("EXTSBR").index("00").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
        ext.set("EXCONO", inCONO)
        
        int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	 
        action.readAll(ext, 1, pageSize, releasedItemProcessor) 
     } 
  }

    Closure<?> releasedItemProcessor = { DBContainer ext -> 
      mi.outData.put("CONO", ext.get("EXCONO").toString())
      mi.outData.put("SUNO", ext.getString("EXSUNO"))
      mi.outData.put("BRND", ext.getString("EXBRND"))
      mi.write() 
   } 
}