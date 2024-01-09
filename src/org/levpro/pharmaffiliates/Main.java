package org.levpro.pharmaffiliates;

import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.levpro.pharmaffiliates.models.Categories;
import org.levpro.pharmaffiliates.models.Childrens;
import org.levpro.pharmaffiliates.models.Products;
import org.levpro.pharmaffiliates.models.Temp;
import org.levpro.pharmaffiliates.utils.PageLoader;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Load input links from file");

        ArrayList<Categories> categoriesResult = new ArrayList<>();
        ArrayList<Products> productsResult = new ArrayList<>();

        File resultDir = new File(System.getProperty("user.dir"), "result");

        if (!resultDir.exists()) {
            resultDir.mkdir();
        }

        System.out.println("Get products data file");

        File productsResultJsonFile = new File(resultDir, "products.json");

        System.out.println("Get temp data file");

        if (productsResultJsonFile.exists()) {
            BufferedReader productsResultBuffer = new BufferedReader(new FileReader(productsResultJsonFile));

            StringBuilder productsResultStringBuffer = new StringBuilder();

            String productsResultLine;

            while ((productsResultLine = productsResultBuffer.readLine()) != null) {
                productsResultStringBuffer.append(productsResultLine);
            }

            productsResultBuffer.close();

            String tempJsonString = productsResultStringBuffer.toString();

            productsResult = new Gson().fromJson(tempJsonString, productsResult.getClass());
        }

        Map<String, Temp> temp = new HashMap<>();

        File tempJsonFile = new File(resultDir, "temp.json");

        if (tempJsonFile.exists()) {
            BufferedReader tempBuffer = new BufferedReader(new FileReader(tempJsonFile));

            StringBuilder tempStringBuffer = new StringBuilder();

            String tempLine;

            while ((tempLine = tempBuffer.readLine()) != null) {
                tempStringBuffer.append(tempLine);
            }

            tempBuffer.close();

            String tempJsonString = tempStringBuffer.toString();

            temp = new Gson().fromJson(tempJsonString, temp.getClass());
        }

        File inputDataFile = new File(System.getProperty("user.dir"), "data.json");

        BufferedReader inputDataBuffer = new BufferedReader(new FileReader(inputDataFile));

        StringBuilder inputDataStringBuffer = new StringBuilder();

        String inputDataLine;

        while ((inputDataLine = inputDataBuffer.readLine()) != null) {
            inputDataStringBuffer.append(inputDataLine);
        }

        inputDataBuffer.close();

        String inputDataJsonString = inputDataStringBuffer.toString();

        Map<String, Object> inputData = new HashMap<>();

        inputData = new Gson().fromJson(inputDataJsonString, inputData.getClass());

        for (Map<String, Object> category : (ArrayList<Map<String, Object>>) inputData.get("categories")) {
            if (temp.containsKey(category.get("url"))) {
                System.out.println(String.format("%s found in temp data", category.get("url")));
            }

            System.out.println("Add to collection");

            categoriesResult.add(new Categories((String) category.get("url"), (String) category.get("name"), "", ""));

            ArrayList<Childrens> childrens = new ArrayList<>();

            ArrayList<String> parsedLinks = new ArrayList<>();

            Temp tempItem = new Temp();

            if (!temp.containsKey(category.get("url"))) {
                System.out.println(String.format("Load %s", category.get("url")));

                String categoryOutput;

                try {
                    categoryOutput = PageLoader.load((String) category.get("url"), "GET");
                } catch (Exception exception) {
                    System.out.println(String.format("Load error %s", exception));

                    continue;
                }

                Document categoryDocument = Jsoup.parse(categoryOutput, "", Parser.xmlParser());

                System.out.println("Get childrens");

                Elements childrensLinks = categoryDocument.select(".alpahbatic-container .cat-box");

                for (Element childrenLink : childrensLinks) {
                    String childrenLinkUrl = childrenLink.select("a").get(0).attr("href").replace(" ", "%20");
                    String childrenLinkName = childrenLink.select("a").get(0).text();

                    System.out.println(String.format("Add children %s - %s", childrenLinkUrl, childrenLinkName));

                    childrens.add(new Childrens(childrenLinkUrl, childrenLinkName));
                }

                System.out.println("Get products");

                Elements productsLinks = categoryDocument.select(".main-page .productbox");

                for (Element productLink : productsLinks) {
                    String productLinkUrl = productLink.select(".product-details .btn-hover").get(0).attr("href").replace(" ", "%20");
                    String productLinkName = productLink.select(".prodcut-title-name h2").get(0).text().replace("\r\n", " ").trim();
                    String productLinkImage = productLink.select(".image-container img").get(0).attr("src");

                    System.out.println(String.format("Add products url %s - %s", productLinkUrl, productLinkName));

                    productsResult.add(new Products(productLinkUrl, (String) category.get("name"), productLinkName, productLinkImage));
                }

                if (!productsResult.isEmpty()) {
                    System.out.println("Save products");

                    FileWriter productsFileWriter = new FileWriter(productsResultJsonFile);
                    productsFileWriter.write(new Gson().toJson(productsResult));
                    productsFileWriter.close();
                }

                System.out.println("Save temp data");

                tempItem.setChildrens(childrens);

                temp.put((String) category.get("url"), tempItem);

                FileWriter tempFileWriter = new FileWriter(tempJsonFile);
                tempFileWriter.write(new Gson().toJson(temp));
                tempFileWriter.close();
            } else {
                System.out.println("Get childrens from temp data");

                childrens.addAll(temp.get(category.get("url")).getChildrens());

                System.out.println("Get early parsed links from temp data");

                parsedLinks.addAll(temp.get(category.get("url")).getParsed());
            }
            
            int childrensIndex = 0;

            while (childrensIndex < childrens.size()) {
                if (parsedLinks.contains(childrens.get(childrensIndex).getUrl())) {
                    System.out.println(String.format("%s parse early", childrens.get(childrensIndex).getUrl()));

                    childrensIndex++;

                    continue;
                }

                System.out.println(String.format("Load %s", childrens.get(childrensIndex).getUrl()));

                String childrenOutput;

                try {
                    childrenOutput = PageLoader.load(childrens.get(childrensIndex).getUrl(), "GET");
                } catch (Exception exception) {
                    System.out.println(String.format("Load error %s", exception));

                    childrensIndex++;

                    continue;
                }

                Document childrenDocument = Jsoup.parse(childrenOutput, "", Parser.xmlParser());

                System.out.println("Get childrens");

                Elements childrensChildrensLinks = childrenDocument.select(".alpahbatic-container .cat-box");

                for (Element childrenChildrenLink : childrensChildrensLinks) {
                    String childrenChildrenUrl = childrenChildrenLink.select("a").get(0).attr("href").replace(" ", "%20");
                    String childrenChildrenName = childrenChildrenLink.select("a").get(0).text();

                    System.out.println(String.format("Add children %s - %s", childrenChildrenUrl, childrenChildrenName));

                    childrens.add(new Childrens(childrenChildrenUrl, childrenChildrenName));
                }

                Elements childrenNameElement = childrenDocument.select(".s1 h1");

                if (childrenNameElement.isEmpty()) {
                    childrenNameElement = childrenDocument.select(".main-page .page-title h1");
                }

                if (childrenNameElement.isEmpty()) {
                    System.out.println("Name not found");

                    childrensIndex++;

                    continue;
                }

                String childrenName = childrenNameElement.get(0).text().replace("\r\n", " ").trim();

                Elements childrenDescriptionElement = childrenDocument.select(".s1 .clearheader");

                if (childrenDescriptionElement.isEmpty()) {
                    childrenDescriptionElement = childrenDocument.select(".s1 .clearheader");
                }

                String childrenDescription = "";

                if (!childrenDescriptionElement.isEmpty()) {
                    childrenDescription = childrenDescriptionElement.get(0).text().replace("\r\n", " ").trim();
                }

                System.out.println("Add to collection");

                categoriesResult.add(new Categories(childrens.get(childrensIndex).getUrl(), childrenName, childrenDescription, childrens.get(childrensIndex).getName()));

                System.out.println("Get products");

                Elements childrenProductsLinks = childrenDocument.select(".main-page .productbox");

                for (Element childrenProductsLink : childrenProductsLinks) {
                    String childrenProductsLinkUrl = childrenProductsLink.select(".product-details .btn-hover").get(0).attr("href").replace(" ", "%20");
                    String childrenProductsLinkName = childrenProductsLink.select(".prodcut-title-name h2").get(0).text().replace("\r\n", " ").trim();
                    String childrenProductsLinkImage = childrenProductsLink.select(".image-container img").get(0).attr("src");

                    System.out.println(String.format("Add products url %s - %s", childrenProductsLinkUrl, childrenProductsLinkName));

                    productsResult.add(new Products(childrenProductsLinkUrl, childrenName, childrenProductsLinkName, childrenProductsLinkImage));
                }

                if (!productsResult.isEmpty()) {
                    System.out.println("Save products");

                    FileWriter productsFileWriter = new FileWriter(productsResultJsonFile);
                    productsFileWriter.write(new Gson().toJson(productsResult));
                    productsFileWriter.close();
                }

                System.out.println("Save temp data");

                tempItem.setChildrens(childrens);
                tempItem.addParsed(childrens.get(childrensIndex).getUrl());

                temp.put((String) category.get("url"), tempItem);

                FileWriter tempFileWriter = new FileWriter(tempJsonFile);
                tempFileWriter.write(new Gson().toJson(temp));
                tempFileWriter.close();

                childrensIndex++;
            }
        }

        System.out.println(String.format("Found %s product and %s categories", productsResult.size(), categoriesResult.size()));

        System.out.println("Save categories");

        File categoriesResultJsonFile = new File(resultDir, "categories.json");

        FileWriter categoresFileWriter = new FileWriter(categoriesResultJsonFile);
        categoresFileWriter.write(new Gson().toJson(categoriesResult));
        categoresFileWriter.close();

        categoriesResult.clear();

        temp.clear();

        ArrayList<String> parsedProductsLinks = new ArrayList<>();

        File parsedProductsLinksJsonFile = new File(resultDir, "parse_products_pinks.json");

        if (parsedProductsLinksJsonFile.exists()) {
            BufferedReader parsedProductsLinksBuffer = new BufferedReader(new FileReader(parsedProductsLinksJsonFile));

            StringBuilder parsedProductsLinksStringBuffer = new StringBuilder();

            String parsedProductsLinksLine;

            while ((parsedProductsLinksLine = parsedProductsLinksBuffer.readLine()) != null) {
                parsedProductsLinksStringBuffer.append(parsedProductsLinksLine);
            }

            parsedProductsLinksBuffer.close();

            String parsedProductsLinksJsonString = parsedProductsLinksStringBuffer.toString();

            parsedProductsLinks = new Gson().fromJson(parsedProductsLinksJsonString, parsedProductsLinks.getClass());
        }

        for (int index = 0; index < productsResult.size(); index++) {
            if (parsedProductsLinks.contains(productsResult.get(index).getUrl())) {
                System.out.println(String.format("%s parsed early", productsResult.get(index).getUrl()));

                continue;
            }

            System.out.println(String.format("Load %s", productsResult.get(index).getUrl()));

            String productOutput;

            try {
                productOutput = PageLoader.load(productsResult.get(index).getUrl(), "GET");
            } catch (Exception exception) {
                System.out.println(String.format("Load error %s", exception));

                continue;
            }

            Document productDocument = Jsoup.parse(productOutput, "", Parser.xmlParser());

            Elements productNameElement = productDocument.select(".title-product h1");

            if (!productNameElement.isEmpty()) {
                productsResult.get(index).setName(productNameElement.get(0).text().replace("\r\n", " ").trim());
            }

            Elements productDescriptionElement = productDocument.select(".single-product .productpage-disclaimer");

            if (!productDescriptionElement.isEmpty()) {
                productsResult.get(index).setDescription(productDescriptionElement.get(0).html().replace("\r\n", " ").trim());
            }

            Elements productSearchKeywordsElement = productDocument.select(".single-product .keywords-container .keywordtext");

            if (!productSearchKeywordsElement.isEmpty()) {
                productsResult.get(index).setSearchKeywords(productSearchKeywordsElement.get(0).html().replace("\r\n", " ").trim());
            }

            Elements productImageElement = productDocument.select(".single-product .product_detail_right .images a");

            if (!productImageElement.isEmpty()) {
                productsResult.get(index).setImage(productImageElement.get(0).attr("href"));
            }

            Elements productParams = productDocument.select(".single-product .product_detail_left_in .shop_table tr");

            for (Element productParam: productParams) {
                productsResult.get(index).addParam(productParam.select("td").get(0).text().replace("\r\n", " ").trim(), productParam.select("td").get(1).text().replace("\r\n", " ").trim());
            }

            System.out.println("Save products");

            FileWriter productsFileWriter = new FileWriter(productsResultJsonFile);
            productsFileWriter.write(new Gson().toJson(productsResult));
            productsFileWriter.close();

            System.out.println("Save parsed links");

            parsedProductsLinks.add(productsResult.get(index).getUrl());

            FileWriter parsedProductsLinksFileWriter = new FileWriter(parsedProductsLinksJsonFile);
            parsedProductsLinksFileWriter.write(new Gson().toJson(parsedProductsLinks));
            parsedProductsLinksFileWriter.close();
        }

        System.out.println("Program complete");
    }
}
