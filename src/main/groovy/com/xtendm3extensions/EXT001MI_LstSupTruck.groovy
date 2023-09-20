// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list supplier trucks from EXTSTR
// Transaction LstSupTruck
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SUNO - Supplier
 * @param: TRCK - Truck
 * 
*/

/**
 * OUT
 * @return : CONO - Company
 * @return : SUNO - Supplier
 * @return : TRCK - Truck
 * @return : TRNA - Name
 * 
*/


public class LstSupTruck extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  int inCONO
  String inSUNO
  String inTRCK
  
  // Constructor 
  public LstSupTruck(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database 
     this.program = program
  } 
    
  public void main() { 
     inCONO = program.LDAZD.CONO as Integer
    
     // Supplier
     if (mi.in.get("SUNO") != null) {
        inSUNO = mi.in.get("SUNO") 
     } else {
        inSUNO = ""     
     }

     // Truck
     if (mi.in.get("TRCK") != null) {
        inTRCK = mi.in.get("TRCK") 
     } else {
        inTRCK = ""     
     }

     // List Supplier Trucks from EXTSTR
     listSupplierTrucks()
  }
 

  //******************************************************************** 
  // List supplier trucks from EXTSTR
  //******************************************************************** 
  void listSupplierTrucks(){ 
     // Read with both SUNO and TRCK as keys if entered 
     if (inSUNO != "" && inTRCK != "") {  
        DBAction action = database.table("EXTSTR").index("00").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
      
        ext.set("EXCONO", inCONO)
        ext.set("EXSUNO", inSUNO)
        ext.set("EXTRCK", inTRCK)

        action.readAll(ext, 3, releasedItemProcessor) 

     } else if (inSUNO != "" && inTRCK == "") {
        DBAction action = database.table("EXTSTR").index("00").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
      
        ext.set("EXCONO", inCONO)
        ext.set("EXSUNO", inSUNO)
     
        action.readAll(ext, 2, releasedItemProcessor) 
        
     } else if (inTRCK != "" && inSUNO == "") {
        DBAction action = database.table("EXTSTR").index("10").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()

        ext.set("EXCONO", inCONO)
        ext.set("EXTRCK", inTRCK)
        
        action.readAll(ext, 2, releasedItemProcessor) 
     } else {
        DBAction action = database.table("EXTSTR").index("00").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()

        ext.set("EXCONO", inCONO)

        action.readAll(ext, 1, releasedItemProcessor) 
     } 
  }

    Closure<?> releasedItemProcessor = { DBContainer ext -> 
      mi.outData.put("CONO", ext.get("EXCONO").toString())
      mi.outData.put("SUNO", ext.getString("EXSUNO"))
      mi.outData.put("TRCK", ext.getString("EXTRCK"))
      mi.outData.put("TRNA", ext.getString("EXTRNA"))
      mi.write() 
   } 
}