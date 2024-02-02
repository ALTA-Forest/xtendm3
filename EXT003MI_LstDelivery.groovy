// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list deliveries from EXTDLH
// Transaction LstDelivery
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: DLTP - Delivery Type
 * @param: STAT - Status
 * @param: DLDT - Delivery date
 * @param: SUNO - Supplier
 * @param: DLST - Delivery Sub Type
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DLNO - Delivery Number
 * @return: DLTP - Delivery Type
 * @return: STAT - Status
 * @return: DLDT - Delivery Date
 * @return: SUNO - Supplier
 * @return: BUYE - Buyer
 * @return: DLTY - Deliver To Yard
 * @return: RCPN - Receipt Number
 * @return: TRPN - Trip Ticket Number
 * @return: TRCK - Truck Number
 * @return: BRND - Brand
 * @return: DLST - Delivery Sub Type
 * @return: CTNO - Contract Number
 * @return: ALHA - Alternate Hauler
 * @return: ISZP - Zero Payment
 * @return: FACI - Facility
 * @return: RVID - Revision Id
 * @return: ISPS - Payee Split
 * @return: VLDT - Validate Date
 * @return: NOTE - Notes
 * 
*/

public class LstDelivery extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inDLTP
  int inSTAT
  int inDLDT
  String inSUNO
  int inDLST
  int numberOfFields
  
  // Constructor 
  public LstDelivery(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
     this.mi = mi
     this.database = database 
     this.program = program
  } 
    
  public void main() { 
     // Set Company Number
     if (mi.in.get("CONO") != null) {
        inCONO = mi.in.get("CONO") 
     } else {
        inCONO = 0      
     }

     // Set Division
     if (mi.in.get("DIVI") != null) {
        inDIVI = mi.in.get("DIVI") 
     } else {
        inDIVI = ""     
     }
    
     // Delivery Number
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0      
     }

     // Delivery Type
     if (mi.in.get("DLTP") != null) {
        inDLTP = mi.in.get("DLTP") 
     } else {
        inDLTP = 0      
     }

     // Status
     if (mi.in.get("STAT") != null) {
        inSTAT = mi.in.get("STAT") 
     } else {
        inSTAT = 0      
     }

     // Delivery Date
     if (mi.in.get("DLDT") != null) {
        inDLDT = mi.in.get("DLDT") 
     } else {
        inDLDT = 0      
     }

     // Supplier
     if (mi.in.get("SUNO") != null) {
        inSUNO = mi.in.get("SUNO") 
     } else {
        inSUNO = ""      
     }
     
     // Delivery Sub Type
     if (mi.in.get("DLST") != null) {
        inDLST = mi.in.get("DLST") 
     } else {
        inDLST = 0      
     }



     // List deliveries from EXTDLH
     listDeliveries()
  }
 
  //******************************************************************** 
  // List Deliveries from EXTDLH
  //******************************************************************** 
  void listDeliveries(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTDLH")

     numberOfFields = 0

     if (inCONO != 0) {
       numberOfFields = 1
       expression = expression.eq("EXCONO", String.valueOf(inCONO))
     }

     if (inDIVI != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXDIVI", inDIVI))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXDIVI", inDIVI)
         numberOfFields = 1
       }
     }

     if (inDLNO != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXDLNO", String.valueOf(inDLNO)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXDLNO", String.valueOf(inDLNO))
         numberOfFields = 1
       }
     }

     if (inDLTP != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXDLTP", String.valueOf(inDLTP)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXDLTP", String.valueOf(inDLTP))
         numberOfFields = 1
       }
     }

     if (inSTAT != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXSTAT", String.valueOf(inSTAT)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXSTAT", String.valueOf(inSTAT))
         numberOfFields = 1
       }
     }

     if (inDLDT != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXDLDT", String.valueOf(inDLDT)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXDLDT", String.valueOf(inDLDT))
         numberOfFields = 1
       }
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

     if (inDLST != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXDLST", String.valueOf(inDLST)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXDLST", String.valueOf(inDLST))
         numberOfFields = 1
       }
     }


     DBAction actionline = database.table("EXTDLH").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()             
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               

   } 

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("DLNO", line.get("EXDLNO").toString())
      mi.outData.put("DLTP", line.get("EXDLTP").toString())
      mi.outData.put("STAT", line.get("EXSTAT").toString())
      mi.outData.put("DLDT", line.get("EXDLDT").toString())
      mi.outData.put("SUNO", line.getString("EXSUNO"))
      mi.outData.put("BUYE", line.getString("EXBUYE"))
      mi.outData.put("DLFY", line.getString("EXDLFY"))
      mi.outData.put("DLTY", line.getString("EXDLTY"))
      mi.outData.put("FDCK", line.get("EXFDCK").toString())
      mi.outData.put("TDCK", line.get("EXTDCK").toString())
      mi.outData.put("RCPN", line.getString("EXRCPN"))
      mi.outData.put("TRPN", line.getString("EXTRPN"))
      mi.outData.put("TRCK", line.getString("EXTRCK"))
      mi.outData.put("BRND", line.getString("EXBRND"))
      mi.outData.put("DLST", line.get("EXDLST").toString())
      mi.outData.put("CTNO", line.get("EXCTNO").toString())
      mi.outData.put("ALHA", line.get("EXALHA").toString())
      mi.outData.put("ISZP", line.get("EXISZP").toString())
      mi.outData.put("FACI", line.getString("EXFACI"))
      mi.outData.put("RVID", line.getString("EXRVID"))
      mi.outData.put("ISPS", line.get("EXISPS").toString())
      mi.outData.put("VLDT", line.getString("EXVLDT"))
      mi.outData.put("NOTE", line.getString("EXNOTE"))
      mi.write() 
   } 
}