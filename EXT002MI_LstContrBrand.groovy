// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list contract brands from EXTCTB
// Transaction LstContrBrand
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: BRND - Brand
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: RVID - Revision ID
 * @return: BRND - Brand
 * 
*/

public class LstContrBrand extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  String inBRND
  String inRVID
  int numberOfFields
  
  // Constructor 
  public LstContrBrand(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
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
        inDIVI = mi.inData.get("DIVI").trim() 
     } else {
        inDIVI = ""     
     }
     
     // Revision ID
     if (mi.in.get("RVID") != null) {
        inRVID = mi.inData.get("RVID") 
     } else {
        inRVID = ""     
     }
     
     // Brand
     if (mi.in.get("BRND") != null) {
        inBRND = mi.inData.get("BRND") 
     } else {
        inBRND = ""     
     }


     // List contract brands
     listContractBrands()
  }
 

  //******************************************************************** 
  // List Contract Brands from EXTCTB
  //******************************************************************** 
  void listContractBrands(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTCTB")

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

     if (inRVID != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXRVID", inRVID))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXRVID", inRVID)
         numberOfFields = 1
       }
     }

     if (inBRND != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXBRND", inBRND))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXBRND", inBRND)
         numberOfFields = 1
       }
     }


     DBAction actionline = database.table("EXTCTB").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()    
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
   } 

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("CBID", line.get("EXCBID").toString())
      mi.outData.put("RVID", line.getString("EXRVID"))
      mi.outData.put("BRND", line.getString("EXBRND"))
      mi.write() 
   }
   
}