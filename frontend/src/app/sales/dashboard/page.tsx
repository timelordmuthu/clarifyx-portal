"use client";

import React from "react";
import Link from "next/link";

/**
 * Sales Dashboard
 * Shows submitted forms and current statuses.
 */
export default function SalesDashboard() {
  // Mock data for UI scaffolding
  const mockForms = [
    { id: 101, customer: "FASTFACTS PRIVATE LIMITED", product: "Web TDS", amount: "₹45,000", status: "SUBMITTED", date: "2026-06-08" },
    { id: 102, customer: "ACME CORP", product: "Desktop", amount: "₹12,000", status: "NEED_MORE_INFO", date: "2026-06-07" },
    { id: 103, customer: "GLOBAL TECH", product: "Web FAMS", amount: "₹80,000", status: "APPROVED", date: "2026-06-05" },
  ];

  return (
    <div className="max-w-6xl mx-auto p-6 mt-10 bg-white shadow rounded-lg">
      <div className="flex justify-between items-center mb-6 border-b pb-4">
        <h1 className="text-2xl font-bold text-gray-800">Sales Dashboard</h1>
        <Link 
            href="/sales/request-creation" 
            className="bg-blue-600 text-white px-4 py-2 rounded shadow hover:bg-blue-700 transition"
        >
          + New Request
        </Link>
      </div>

      <div className="overflow-x-auto">
        <table className="min-w-full bg-white border border-gray-200">
          <thead className="bg-gray-50 text-left">
            <tr>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Req ID</th>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Date</th>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Customer</th>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Product</th>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Amount</th>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Status</th>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Action</th>
            </tr>
          </thead>
          <tbody>
            {mockForms.map((form) => (
              <tr key={form.id} className="hover:bg-gray-50">
                <td className="py-3 px-4 border-b text-gray-800">#{form.id}</td>
                <td className="py-3 px-4 border-b text-gray-600">{form.date}</td>
                <td className="py-3 px-4 border-b text-gray-800 font-medium">{form.customer}</td>
                <td className="py-3 px-4 border-b text-gray-600">{form.product}</td>
                <td className="py-3 px-4 border-b text-gray-600">{form.amount}</td>
                <td className="py-3 px-4 border-b">
                  <span className={`px-2 py-1 text-xs rounded font-semibold
                    ${form.status === 'APPROVED' ? 'bg-green-100 text-green-800' : ''}
                    ${form.status === 'SUBMITTED' ? 'bg-blue-100 text-blue-800' : ''}
                    ${form.status === 'NEED_MORE_INFO' ? 'bg-yellow-100 text-yellow-800' : ''}
                  `}>
                    {form.status.replace(/_/g, ' ')}
                  </span>
                </td>
                <td className="py-3 px-4 border-b">
                  {form.status === "NEED_MORE_INFO" ? (
                    <button className="text-blue-600 hover:text-blue-800 font-medium underline">
                      Edit & Resubmit
                    </button>
                  ) : (
                    <button className="text-gray-500 hover:text-gray-700 font-medium">
                      View
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
