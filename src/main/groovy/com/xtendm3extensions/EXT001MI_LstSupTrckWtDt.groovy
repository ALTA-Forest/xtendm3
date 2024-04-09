// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list supplier truck weight history from EXTTWH based on date
// Transaction LstSupTrckWtDt
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SUNO - Supplier
 * @param: TRCK - Truck
 * @param: DLDT - Delivery Date 
 * 
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


public class LstSupTrckWtDt extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  int inCONO
  String inSUNO
  String inTRCK
  int inDLDT
  
  // Constructor 
  public LstSupTrckWtDt(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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

     // Delivery Date
     if (mi.in.get("DLDT") != null) {
        inDLDT = mi.in.get("DLDT") 
     } else {
        inDLDT = 0    
     }


     // List Supplier Trucks from EXTTWH
     listSupplierTruckWeightHistory()
  }
 
 

  //******************************************************************** 
  // List supplier truck weight history from EXTTWH
  //******************************************************************** 
  void listSupplierTruckWeightHistory() { 
     DBAction query = database.table("EXTTWH").index("00").selectAllFields().build()
     DBContainer ext = query.getContainer()
     ext.set("EXCONO", inCONO)
     ext.set("EXSUNO", inSUNO)
     ext.set("EXTRCK", inTRCK)
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	 
     query.readAll(ext, 3, pageSize, releasedItemProcessor)
  }

  Closure<?> releasedItemProcessor = { DBContainer ext -> 
    int fromDate = ext.get("EXFRDT")
    int toDate = ext.get("EXTODT")
      
    if ((inDLDT >= fromDate) && (inDLDT <= toDate) ) {
      mi.outData.put("CONO", ext.get("EXCONO").toString())
      mi.outData.put("SUNO", ext.getString("EXSUNO"))
      mi.outData.put("TRCK", ext.getString("EXTRCK"))
      mi.outData.put("TARE", ext.get("EXTARE").toString())
      mi.outData.put("FRDT", ext.get("EXFRDT").toString())
      mi.outData.put("TODT", ext.get("EXTODT").toString())
      mi.write() 
    }
  }
}