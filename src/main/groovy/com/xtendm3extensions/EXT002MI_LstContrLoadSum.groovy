// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-06-07
// @version   1.0 
//
// Description 
// This API is to list contract loads from EXTCTL
// Transaction LstContrLoadSum
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: CTNO - Contract Number
 * @param: RVID - Revision ID
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DLNO - Delivery Number
 * @return: CTNO - Contract Number
 * @return: RVID - Revision ID
 * @return: TNLB - Total Net lbs
 * @return: TNBF - Total Net bf
 * @return: AVBL - Average bf/lbs
 * 
*/

public class LstContrLoadSum extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inCTNO
  String inRVID
  int numberOfFields
  Integer outCONO
  String outDIVI
  int outDLNO
  int outCTNO
  String outRVID
  double outTNLB
  double outTNBF
  double outAVBL

  
  // Constructor 
  public LstContrLoadSum(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database 
     this.program = program
     this.logger = logger
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

     // Delivery Number
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0     
     }

     // Contract Number
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } else {
        inCTNO = 0     
     }
    
     // Revision ID
     if (mi.in.get("RVID") != null) {
        inRVID = mi.inData.get("RVID").trim() 
     } else {
        inRVID = ""     
     }
     

     // List contract brands
     listContractInstructions()
     
     setOutput()
     mi.write() 
  }



  //******************************************************************** 
  // List Contract Loads from EXTCTL
  //******************************************************************** 
  void listContractInstructions(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTCTL")

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

     if (inCTNO != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXCTNO", String.valueOf(inCTNO)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXCTNO", String.valueOf(inCTNO))
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
     
     String index
     if (numberOfFields == 0) {
        index = "10"
     } else {
        index = "00"
     }
       
     DBAction actionline = database.table("EXTCTL").index(index).matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
   } 


    Closure<?> releasedLineProcessor = { DBContainer line -> 
      outCONO = line.get("EXCONO")
      outDIVI = line.getString("EXDIVI")
      outDLNO = line.get("EXDLNO")
      outCTNO = line.get("EXCTNO")
      outRVID = line.getString("EXRVID")
      Double sumTNLB = line.getDouble("EXTNLB")
      outTNLB = outTNLB + sumTNLB
      Double sumTNBF = line.getDouble("EXTNBF")
      outTNBF = outTNBF + sumTNBF
      Double sumAVBL = outTNBF/outTNLB
      outAVBL = sumAVBL.round(5)
   } 
   
  
  //******************************************************************** 
  // Set Output data
  //******************************************************************** 
  void setOutput() {
      mi.outData.put("CONO", outCONO.toString())
      mi.outData.put("DIVI", outDIVI.toString())
      if (mi.in.get("DLNO") != null && mi.in.get("DLNO") != "") {
         mi.outData.put("DLNO", outDLNO.toString())
      }
      if (mi.in.get("CTNO") != null && mi.in.get("CTNO") != "") {
         mi.outData.put("CTNO", outCTNO.toString())
      }
      if (mi.in.get("RVID") != null && mi.in.get("RVID") != "") {
         mi.outData.put("RVID", outRVID.toString())
      }
      mi.outData.put("TNLB", outTNLB.toString())
      mi.outData.put("TNBF", outTNBF.toString())
      mi.outData.put("AVBL", outAVBL.toString())
      
      if (numberOfFields == 0) {
         mi.outData.put("CTNO", outCTNO.toString())
      }
  } 
   
}