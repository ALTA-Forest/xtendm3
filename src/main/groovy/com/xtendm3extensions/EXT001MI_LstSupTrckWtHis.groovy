// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list supplier truck weight history from EXTTWH
// Transaction LstSupTrckWtHis
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SUNO - Supplier
 * @param: TRCK - Truck
 * @param : FRDT - From Date
 * @param : TODT - To Date
*/

/**
 * OUT
 * @return : CONO - Company
 * @return : SUNO - Supplier
 * @return : TRCK - Truck
 * @return : TARE - Tare
 * @return : FRDT - From Date
 * @return : TODT - To Date
 * 
*/


public class LstSupTrckWtHis extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  int inCONO
  String inSUNO
  String inTRCK
  int inFRDT
  int inTODT
  int numberOfFields
  
  // Constructor 
  public LstSupTrckWtHis(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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

     // From Date
     if (mi.in.get("FRDT") != null) {
        inFRDT = mi.in.get("FRDT") 
     } else {
        inFRDT = 0    
     }

     // To Date
     if (mi.in.get("TODT") != null) {
        inTODT = mi.in.get("TODT") 
     } else {
        inTODT = 0     
     }

     // List Supplier Trucks from EXTTWH
     listSupplierTruckWeightHistory()
  }
 
  //******************************************************************** 
  // List supplier truck weight history from EXTTWH
  //******************************************************************** 
  void listSupplierTruckWeightHistory(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTTWH")

     numberOfFields = 0

     if (inCONO != 0) {
       numberOfFields = 1
       expression = expression.eq("EXCONO", String.valueOf(inCONO))
     }

     if (inSUNO != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXSUNO", inSUNO))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXSUNO", inSUNO)
         numberOfFields = 1
       }
     }

     if (inTRCK != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXTRCK", inTRCK))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXTRCK", inTRCK)
         numberOfFields = 1
       }
     }

     if (inFRDT != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXFRDT", String.valueOf(inFRDT)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXFRDT", String.valueOf(inFRDT))
         numberOfFields = 1
       }
     }

     if (inTODT != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXTODT", String.valueOf(inTODT)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXTODT", String.valueOf(inTODT))
         numberOfFields = 1
       }
     }


     DBAction actionline = database.table("EXTTWH").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 ? 1000 : mi.getMaxRecords()          
     
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               

   } 

  //******************************************************************** 
  // List supplier truck weight history from EXTTWH
  //******************************************************************** 
  /*void listSupplierTruckWeightHistory(){ 
     // Read with both SUNO and TRCK as keys if entered 
     if (inSUNO != "" && inTRCK != "") {  
        DBAction action = database.table("EXTTWH").index("00").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
      
        ext.set("EXCONO", inCONO)
        ext.set("EXSUNO", inSUNO)
        ext.set("EXTRCK", inTRCK)

        action.readAll(ext, 3, releasedItemProcessor) 

     } else if (inSUNO != "" && inTRCK == "") {
        DBAction action = database.table("EXTTWH").index("00").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()
      
        ext.set("EXCONO", inCONO)
        ext.set("EXSUNO", inSUNO)
     
        action.readAll(ext, 2, releasedItemProcessor) 
        
     } else if (inTRCK != "" && inSUNO == "") {
        DBAction action = database.table("EXTTWH").index("10").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()

        ext.set("EXCONO", inCONO)
        ext.set("EXTRCK", inTRCK)
        
        action.readAll(ext, 2, releasedItemProcessor) 
     } else {
        DBAction action = database.table("EXTTWH").index("00").selectAllFields().reverse().build()
        DBContainer ext = action.getContainer()

        ext.set("EXCONO", inCONO)

        action.readAll(ext, 1, releasedItemProcessor) 
     } 
  }*/

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("SUNO", line.getString("EXSUNO"))
      mi.outData.put("TRCK", line.getString("EXTRCK"))
      mi.outData.put("TARE", line.get("EXTARE").toString())
      mi.outData.put("FRDT", line.get("EXFRDT").toString())
      mi.outData.put("TODT", line.get("EXTODT").toString())
      mi.write() 
   } 
}