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
     // Set Company Number
     inCONO = program.LDAZD.CONO as Integer
    
     // Supplier
     if (mi.in.get("SUNO") != null && mi.in.get("SUNO") != "") {
        inSUNO = mi.inData.get("SUNO").trim() 
     } else {
        inSUNO = ""     
     }

     // Truck
     if (mi.in.get("TRCK") != null && mi.in.get("TRCK") != "") {
        inTRCK = mi.inData.get("TRCK").trim() 
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
     line.set("EXCONO", inCONO)
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	       
     actionline.readAll(line, 1, pageSize, releasedLineProcessor)               

   } 

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